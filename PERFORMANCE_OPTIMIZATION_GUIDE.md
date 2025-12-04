# 性能优化实施指南

本文档提供了 HealthCheck 项目性能优化的详细实施方案，涵盖数据库、缓存、API和前端等各个层面。

---

## 1. 数据库优化

### 1.1 索引优化

#### 当前问题分析

```sql
-- ❌ 没有索引，导致全表扫描
SELECT * FROM medical_literature WHERE title LIKE '%covid%';
SELECT * FROM examination_order WHERE status = '待检查' ORDER BY create_time DESC;
```

#### 优化方案

创建 `database_optimization.sql`:

```sql
-- ========================================
-- Medical Literature 表优化
-- ========================================

-- 1. 标题全文索引（支持中英文搜索）
ALTER TABLE medical_literature ADD FULLTEXT INDEX idx_title_fulltext (title);
ALTER TABLE medical_literature ADD FULLTEXT INDEX idx_summary_fulltext (summary);

-- 2. 常用查询字段索引
ALTER TABLE medical_literature ADD INDEX idx_source (source);
ALTER TABLE medical_literature ADD INDEX idx_category (category);
ALTER TABLE medical_literature ADD INDEX idx_status (status);
ALTER TABLE medical_literature ADD INDEX idx_create_time (create_time);

-- 3. 复合索引（优化组合查询）
ALTER TABLE medical_literature ADD INDEX idx_source_category_status (source, category, status);
ALTER TABLE medical_literature ADD INDEX idx_status_create_time (status, create_time DESC);

-- ========================================
-- Examination Order 表优化
-- ========================================

-- 1. 单列索引
ALTER TABLE examination_order ADD INDEX idx_user_id (user_id);
ALTER TABLE examination_order ADD INDEX idx_status (status);
ALTER TABLE examination_order ADD INDEX idx_order_type (order_type);
ALTER TABLE examination_order ADD INDEX idx_create_time (create_time);

-- 2. 复合索引（优化统计查询）
ALTER TABLE examination_order ADD INDEX idx_status_create_time (status, create_time DESC);
ALTER TABLE examination_order ADD INDEX idx_user_status (user_id, status);
ALTER TABLE examination_order ADD INDEX idx_type_status_time (order_type, status, create_time);

-- ========================================
-- User 表优化
-- ========================================

-- 1. 唯一索引（用户名查询）
ALTER TABLE `user` ADD UNIQUE INDEX idx_username (username);
ALTER TABLE `user` ADD INDEX idx_phone (phone);
ALTER TABLE `user` ADD INDEX idx_email (email);

-- ========================================
-- Doctor 表优化
-- ========================================

ALTER TABLE doctor ADD INDEX idx_title_id (title_id);
ALTER TABLE doctor ADD INDEX idx_office_id (office_id);
ALTER TABLE doctor ADD INDEX idx_username (username);

-- ========================================
-- AI Consultation 表优化
-- ========================================

ALTER TABLE ai_consultation ADD INDEX idx_user_id (user_id);
ALTER TABLE ai_consultation ADD INDEX idx_create_time (create_time);
ALTER TABLE ai_consultation ADD INDEX idx_user_time (user_id, create_time DESC);

-- ========================================
-- 查看索引使用情况
-- ========================================

-- 查看表的索引
SHOW INDEX FROM medical_literature;

-- 分析慢查询
EXPLAIN SELECT * FROM medical_literature 
WHERE status = 'active' AND source = 'arxiv' 
ORDER BY create_time DESC LIMIT 10;
```

#### 验证索引效果

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1; -- 1秒以上记录为慢查询

-- 查看执行计划
EXPLAIN EXTENDED 
SELECT * FROM medical_literature 
WHERE status = 'active' AND category = 'cardiology'
ORDER BY create_time DESC LIMIT 20;

-- 应该看到 type = ref 或 range，而不是 ALL
```

### 1.2 查询优化

#### 问题：WebController中的N+1查询

```java
// ❌ 当前代码 - 在循环中查询数据库
@GetMapping("/pieData")
public Result getPieData() {
    List<Title> titleList = titleService.selectAll(null);
    List<Map<String, Object>> list = new ArrayList<>();
    for (Title title : titleList) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", title.getName());
        Integer count = doctorService.selectByTitleId(title.getId()); // N次查询！
        map.put("value", count);
        list.add(map);
    }
    return Result.success(list);
}
```

#### 优化方案1：批量查询

```java
// ✅ 优化后 - 一次性查询所有数据
@GetMapping("/pieData")
public Result getPieData() {
    List<Title> titleList = titleService.selectAll(null);
    
    // 获取所有titleId
    List<Integer> titleIds = titleList.stream()
            .map(Title::getId)
            .collect(Collectors.toList());
    
    // 一次查询获取所有统计数据
    Map<Integer, Integer> countMap = doctorService.countByTitleIds(titleIds);
    
    // 组装结果
    List<Map<String, Object>> list = titleList.stream()
            .map(title -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", title.getName());
                map.put("value", countMap.getOrDefault(title.getId(), 0));
                return map;
            })
            .collect(Collectors.toList());
    
    return Result.success(list);
}
```

```java
// DoctorService.java - 新增批量查询方法
public Map<Integer, Integer> countByTitleIds(List<Integer> titleIds) {
    if (titleIds == null || titleIds.isEmpty()) {
        return Collections.emptyMap();
    }
    
    List<Map<String, Object>> results = doctorMapper.countGroupByTitleIds(titleIds);
    
    return results.stream()
            .collect(Collectors.toMap(
                    row -> (Integer) row.get("titleId"),
                    row -> ((Long) row.get("count")).intValue()
            ));
}
```

```xml
<!-- DoctorMapper.xml -->
<select id="countGroupByTitleIds" resultType="map">
    SELECT title_id as titleId, COUNT(*) as count
    FROM doctor
    WHERE title_id IN
    <foreach collection="list" item="id" open="(" close=")" separator=",">
        #{id}
    </foreach>
    GROUP BY title_id
</select>
```

#### 优化方案2：JOIN查询

```xml
<!-- TitleMapper.xml - 使用JOIN一次性获取 -->
<select id="selectAllWithDoctorCount" resultType="map">
    SELECT 
        t.id,
        t.name,
        COUNT(d.id) as doctorCount
    FROM title t
    LEFT JOIN doctor d ON t.id = d.title_id
    GROUP BY t.id, t.name
    ORDER BY t.name
</select>
```

```java
// 简化后的Controller
@GetMapping("/pieData")
public Result getPieData() {
    List<Map<String, Object>> list = titleService.selectAllWithDoctorCount();
    return Result.success(list);
}
```

### 1.3 分页优化

#### 问题：全表查询后内存分页

```java
// ❌ 当前代码 - 先查所有，再分页（内存占用大）
List<ExaminationOrder> allOrders = examinationOrderMapper.selectAll(null);
// ... 业务处理
```

#### 优化方案：数据库分页

```java
// ✅ 使用PageHelper分页
@GetMapping("/lineData")
public Result getLineData(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "100") int size) {
    
    PageHelper.startPage(page, size);
    List<ExaminationOrder> orders = examinationOrderMapper.selectRecentOrders(30);
    PageInfo<ExaminationOrder> pageInfo = new PageInfo<>(orders);
    
    // ... 业务处理
    
    return Result.success(data);
}
```

```xml
<!-- ExaminationOrderMapper.xml -->
<select id="selectRecentOrders" resultType="ExaminationOrder">
    SELECT id, user_id, money, status, create_time
    FROM examination_order
    WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
    ORDER BY create_time DESC
</select>
```

### 1.4 批量操作优化

```java
// ✅ MedicalLiteratureService - 真正的批量插入
@Transactional(rollbackFor = Exception.class)
public int batchAdd(List<MedicalLiterature> literatures) {
    if (CollectionUtils.isEmpty(literatures)) {
        return 0;
    }
    
    // 设置默认值
    Date now = new Date();
    literatures.forEach(lit -> {
        lit.setCreateTime(now);
        if (lit.getStatus() == null) {
            lit.setStatus("active");
        }
    });
    
    // 使用批量插入（JDBC batch）
    int batchSize = 100;
    int savedCount = 0;
    
    for (int i = 0; i < literatures.size(); i += batchSize) {
        int end = Math.min(i + batchSize, literatures.size());
        List<MedicalLiterature> batch = literatures.subList(i, end);
        
        try {
            mapper.batchInsert(batch); // 真正的批量插入
            savedCount += batch.size();
        } catch (Exception e) {
            log.error("批量插入失败: batch {}-{}", i, end, e);
            // 回退到逐条插入
            savedCount += insertOneByOne(batch);
        }
    }
    
    return savedCount;
}
```

```xml
<!-- MedicalLiteratureMapper.xml - 批量插入 -->
<insert id="batchInsert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO medical_literature (title, authors, source, category, summary, link, create_time, status)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.title}, #{item.authors}, #{item.source}, #{item.category}, 
         #{item.summary}, #{item.link}, #{item.createTime}, #{item.status})
    </foreach>
</insert>
```

---

## 2. 缓存优化

### 2.1 扩大缓存覆盖范围

#### 用户信息缓存

```java
@Service
public class UserService {
    
    /**
     * 缓存用户信息（5分钟）
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 更新用户时清除缓存
     */
    @CacheEvict(value = "users", key = "#user.id")
    public void updateById(User user) {
        userMapper.updateById(user);
    }
    
    /**
     * 删除用户时清除缓存
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }
}
```

#### 医生列表缓存

```java
@Service
public class DoctorService {
    
    /**
     * 缓存医生列表（10分钟）
     */
    @Cacheable(value = "doctors", key = "'all'")
    public List<Doctor> selectAll(Doctor doctor) {
        return doctorMapper.selectAll(doctor);
    }
    
    /**
     * 按职称缓存
     */
    @Cacheable(value = "doctors", key = "'title_' + #titleId")
    public List<Doctor> selectByTitleId(Integer titleId) {
        return doctorMapper.selectByTitleId(titleId);
    }
    
    /**
     * 更新/删除医生时清空所有医生缓存
     */
    @CacheEvict(value = "doctors", allEntries = true)
    public void updateById(Doctor doctor) {
        doctorMapper.updateById(doctor);
    }
}
```

#### 配置缓存TTL

```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 默认10分钟
    cache-names:
      - users:300000        # 用户缓存5分钟
      - doctors:600000      # 医生缓存10分钟
      - packages:1800000    # 套餐缓存30分钟
      - literature:3600000  # 文献缓存1小时
```

### 2.2 缓存预热

```java
@Component
@Slf4j
public class CacheWarmUpTask {
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private ExaminationPackageService packageService;
    
    @Autowired
    private TitleService titleService;
    
    /**
     * 应用启动时预热缓存
     */
    @PostConstruct
    public void warmUpCache() {
        log.info("开始缓存预热...");
        
        try {
            // 预热医生列表
            doctorService.selectAll(null);
            log.info("医生列表缓存预热完成");
            
            // 预热体检套餐
            packageService.selectAll(null);
            log.info("体检套餐缓存预热完成");
            
            // 预热职称列表
            titleService.selectAll(null);
            log.info("职称列表缓存预热完成");
            
            log.info("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    /**
     * 每天凌晨3点刷新缓存
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshCache() {
        log.info("定时刷新缓存...");
        warmUpCache();
    }
}
```

### 2.3 缓存穿透防护

```java
@Service
public class MedicalLiteratureService {
    
    /**
     * 使用布隆过滤器防止缓存穿透
     */
    @Autowired
    private BloomFilter<String> literatureBloomFilter;
    
    public MedicalLiterature findByTitle(String title) {
        // 1. 布隆过滤器快速判断
        if (!literatureBloomFilter.mightContain(title)) {
            log.info("布隆过滤器判断数据不存在: {}", title);
            return null;
        }
        
        // 2. 查询缓存
        String cacheKey = "literature:" + title;
        MedicalLiterature cached = cacheService.get(cacheKey, MedicalLiterature.class);
        if (cached != null) {
            return cached;
        }
        
        // 3. 查询数据库
        MedicalLiterature literature = mapper.selectByTitle(title);
        
        // 4. 缓存结果（包括null，防止缓存穿透）
        if (literature != null) {
            cacheService.set(cacheKey, literature, 30, TimeUnit.MINUTES);
        } else {
            // 缓存空值，但TTL较短
            cacheService.set(cacheKey, new NullValue(), 5, TimeUnit.MINUTES);
        }
        
        return literature;
    }
}
```

---

## 3. API 响应时间优化

### 3.1 异步处理

#### 爬虫任务异步化

```java
@Service
public class CrawlerAsyncService {
    
    @Async("crawlerExecutor")
    public CompletableFuture<CrawlResult> crawlAsync(CrawlRequest request) {
        try {
            CrawlResult result = orchestrator.crawl(request);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步爬取失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "crawlerExecutor")
    public Executor crawlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("crawler-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

```java
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {
    
    @Autowired
    private CrawlerAsyncService crawlerAsyncService;
    
    private Map<String, CompletableFuture<CrawlResult>> taskMap = new ConcurrentHashMap<>();
    
    /**
     * 异步启动爬虫任务
     */
    @PostMapping("/start-async")
    public Result startCrawlAsync(@RequestBody CrawlRequest request) {
        String taskId = UUID.randomUUID().toString();
        
        CompletableFuture<CrawlResult> future = crawlerAsyncService.crawlAsync(request);
        taskMap.put(taskId, future);
        
        // 5分钟后自动清理
        future.thenRun(() -> {
            scheduledExecutor.schedule(() -> taskMap.remove(taskId), 5, TimeUnit.MINUTES);
        });
        
        return Result.success(Map.of("taskId", taskId, "status", "running"));
    }
    
    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public Result getTaskStatus(@PathVariable String taskId) {
        CompletableFuture<CrawlResult> future = taskMap.get(taskId);
        
        if (future == null) {
            return Result.error("404", "任务不存在");
        }
        
        if (!future.isDone()) {
            return Result.success(Map.of("status", "running"));
        }
        
        try {
            CrawlResult result = future.get();
            return Result.success(Map.of("status", "completed", "result", result));
        } catch (Exception e) {
            return Result.error("500", "任务失败: " + e.getMessage());
        }
    }
}
```

### 3.2 API 限流

```java
@Configuration
public class RateLimiterConfig {
    
    @Bean
    public RateLimiter crawlerRateLimiter() {
        return RateLimiter.of("crawlerAPI", RateLimiterConfig.custom()
                .limitForPeriod(10)           // 每个周期最多10个请求
                .limitRefreshPeriod(Duration.ofMinutes(1))  // 周期1分钟
                .timeoutDuration(Duration.ofSeconds(5))     // 等待5秒
                .build());
    }
}

@RestController
public class CrawlerController {
    
    @Autowired
    private RateLimiter crawlerRateLimiter;
    
    @PostMapping("/api/crawler/start")
    public Result startCrawl(@RequestBody CrawlRequest request) {
        try {
            // 限流检查
            RateLimiter.waitForPermission(crawlerRateLimiter);
            
            CrawlResult result = orchestrator.crawl(request);
            return Result.success(result);
            
        } catch (RequestNotPermitted e) {
            return Result.error("429", "请求过于频繁，请稍后再试");
        }
    }
}
```

### 3.3 数据库连接池优化

```yaml
# application.yml - HikariCP优化配置
spring:
  datasource:
    hikari:
      pool-name: HealthCheckPool
      minimum-idle: 10              # 最小空闲连接
      maximum-pool-size: 50         # 最大连接数（根据实际负载调整）
      connection-timeout: 20000     # 连接超时20秒
      idle-timeout: 300000          # 空闲连接超时5分钟
      max-lifetime: 1200000         # 连接最大生命周期20分钟
      connection-test-query: SELECT 1
      auto-commit: true
      leak-detection-threshold: 60000  # 连接泄漏检测1分钟
      
      # 性能优化
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

---

## 4. 前端性能优化

### 4.1 Vite 构建优化

```javascript
// vite.config.js - 完整优化配置
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { visualizer } from 'rollup-plugin-visualizer'
import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // Gzip压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,  // 10KB以上压缩
      algorithm: 'gzip',
      ext: '.gz',
    }),
    // 打包分析
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true,
    }),
  ],
  
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'axios',
      'element-plus',
      'echarts/core',
      'echarts/charts',
      'echarts/components',
    ],
  },
  
  build: {
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,      // 移除console
        drop_debugger: true,     // 移除debugger
        pure_funcs: ['console.log']  // 移除特定函数
      }
    },
    
    // 分包策略
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'echarts': ['echarts'],
          'editor': ['@wangeditor/editor', '@wangeditor/editor-for-vue'],
          'utils': ['axios']
        },
        // 代码分割命名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    },
    
    // 块大小警告限制
    chunkSizeWarningLimit: 1000,
    
    // 启用CSS代码分割
    cssCodeSplit: true,
    
    // 构建时报告压缩大小
    reportCompressedSize: true,
  },
  
  // 生产环境移除console
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : []
  },
})
```

### 4.2 图片优化

```javascript
// src/utils/imageOptimizer.js
/**
 * 图片懒加载
 */
export function setupLazyLoad() {
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target
          img.src = img.dataset.src
          img.classList.remove('lazy')
          imageObserver.unobserve(img)
        }
      })
    })
    
    document.querySelectorAll('img.lazy').forEach(img => {
      imageObserver.observe(img)
    })
  }
}

/**
 * 图片压缩
 */
export function compressImage(file, maxWidth = 800, quality = 0.8) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        
        let width = img.width
        let height = img.height
        
        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width)
          width = maxWidth
        }
        
        canvas.width = width
        canvas.height = height
        
        ctx.drawImage(img, 0, 0, width, height)
        
        canvas.toBlob((blob) => {
          resolve(new File([blob], file.name, {
            type: 'image/jpeg',
            lastModified: Date.now()
          }))
        }, 'image/jpeg', quality)
      }
    }
  })
}
```

### 4.3 路由懒加载优化

```javascript
// src/router/index.js - 优化后
import { createRouter, createWebHistory } from 'vue-router'

// 使用 Vite 的动态导入
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/manager',
      component: () => import(/* webpackChunkName: "manager" */ '@/views/Manager.vue'),
      children: [
        {
          path: 'home',
          component: () => import(/* webpackChunkName: "home" */ '@/views/manager/Home.vue'),
        },
        {
          path: 'medical-literature',
          component: () => import(/* webpackChunkName: "literature" */ '@/views/manager/MedicalLiterature.vue'),
        },
        // 预加载高频页面
        {
          path: 'user',
          component: () => import(/* webpackPrefetch: true */ '@/views/manager/User.vue'),
        },
      ]
    }
  ]
})

// 路由加载进度条
router.beforeEach((to, from, next) => {
  // 显示加载进度
  NProgress.start()
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
```

### 4.4 API请求优化

```javascript
// src/utils/request.js - 优化版
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 30000
})

// 请求队列管理
const pendingRequests = new Map()

// 生成请求唯一标识
function getRequestKey(config) {
  return `${config.method}_${config.url}_${JSON.stringify(config.params)}`
}

// 取消重复请求
function removePendingRequest(config) {
  const requestKey = getRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    const cancel = pendingRequests.get(requestKey)
    cancel('取消重复请求')
    pendingRequests.delete(requestKey)
  }
}

// 请求拦截器
request.interceptors.request.use(config => {
  // 取消之前的相同请求
  removePendingRequest(config)
  
  // 添加取消令牌
  config.cancelToken = new axios.CancelToken(cancel => {
    pendingRequests.set(getRequestKey(config), cancel)
  })
  
  // 添加token
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  config.headers['token'] = user.token || ''
  
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    removePendingRequest(response.config)
    return response.data
  },
  error => {
    // 如果是取消请求，不显示错误
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    
    removePendingRequest(error.config || {})
    
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
```

---

## 5. 性能监控

### 5.1 后端性能监控

```java
@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {
    
    @Around("execution(* com.example.service.*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > 1000) {
                log.warn("慢方法检测: {} 耗时 {}ms", methodName, duration);
            } else {
                log.debug("方法执行: {} 耗时 {}ms", methodName, duration);
            }
            
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("方法异常: {} 耗时 {}ms", methodName, duration, e);
            throw e;
        }
    }
}
```

### 5.2 前端性能监控

```javascript
// src/utils/performance.js
export class PerformanceMonitor {
  static init() {
    if (typeof window === 'undefined') return
    
    // 页面加载性能
    window.addEventListener('load', () => {
      const perfData = window.performance.timing
      const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart
      const dnsTime = perfData.domainLookupEnd - perfData.domainLookupStart
      const tcpTime = perfData.connectEnd - perfData.connectStart
      const requestTime = perfData.responseEnd - perfData.requestStart
      const renderTime = perfData.domComplete - perfData.domLoading
      
      console.log('Performance Metrics:', {
        pageLoadTime: `${pageLoadTime}ms`,
        dnsTime: `${dnsTime}ms`,
        tcpTime: `${tcpTime}ms`,
        requestTime: `${requestTime}ms`,
        renderTime: `${renderTime}ms`
      })
      
      // 发送到监控服务
      // this.sendMetrics({ pageLoadTime, dnsTime, tcpTime, requestTime, renderTime })
    })
    
    // 长任务监控
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.duration > 50) {
            console.warn('Long Task detected:', entry)
          }
        }
      })
      observer.observe({ entryTypes: ['longtask'] })
    }
  }
  
  static measureRouteChange(to, from) {
    const start = performance.now()
    return () => {
      const duration = performance.now() - start
      console.log(`Route change from ${from.path} to ${to.path}: ${duration}ms`)
    }
  }
}
```

---

## 6. 压力测试

### 6.1 使用 Apache Bench

```bash
# 测试登录接口
ab -n 1000 -c 100 -p login.json -T application/json http://localhost:9090/login

# 测试查询接口
ab -n 5000 -c 200 http://localhost:9090/api/literature?page=1&size=10

# 测试爬虫接口
ab -n 100 -c 10 -p crawl.json -T application/json http://localhost:9090/api/crawler/start
```

### 6.2 使用 JMeter

创建 `performance-test.jmx` 测试计划：

1. 并发用户数：100-500
2. 持续时间：10分钟
3. 目标QPS：1000

### 6.3 性能基准

| 接口 | 目标响应时间(P95) | 目标QPS |
|------|------------------|---------|
| 登录 | < 200ms | 500 |
| 查询用户列表 | < 300ms | 1000 |
| 查询文献列表 | < 500ms | 500 |
| 创建订单 | < 1s | 200 |
| 启动爬虫 | < 2s | 10 |

---

## 7. 优化效果预期

| 优化项 | 优化前 | 优化后 | 提升 |
|--------|--------|--------|------|
| 首页加载时间 | ~3s | <1s | 67% |
| API平均响应时间 | ~1.5s | <500ms | 67% |
| 数据库查询时间 | ~500ms | <100ms | 80% |
| 并发支持 | ~100 | ~500 | 400% |
| 内存占用 | ~1GB | ~500MB | 50% |

---

**下一步**: 完成性能优化后，进行压力测试验证效果。
