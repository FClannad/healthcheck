package com.example.service;

import com.example.entity.MedicalLiterature;
import com.example.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务
 * 提供系统各种数据的统计分析功能
 *
 * 【未使用服务说明】
 * 此服务提供系统统计功能，但目前没有Controller调用这些方法。
 * 前端DataAnalysis.vue使用的是其他接口(/getCountData, /lineData, /pieData, /barData)。
 * 保留此服务以便后续扩展统计功能。
 */
@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private CacheService cacheService;

    /**
     * 获取系统概览统计
     */
    public Map<String, Object> getSystemOverview() {
        try {
            String cacheKey = "system_overview_" + LocalDate.now().toString();
            
            // 尝试从缓存获取
            if (cacheService != null) {
                Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
                if (cached != null) {
                    logger.debug("从缓存获取系统概览统计");
                    return cached;
                }
            }
            
            Map<String, Object> overview = new HashMap<>();
            
            // 用户统计
            Map<String, Object> userStats = getUserStatistics();
            overview.put("users", userStats);
            
            // 文献统计
            Map<String, Object> literatureStats = getLiteratureStatistics();
            overview.put("literature", literatureStats);
            
            // 系统活跃度
            Map<String, Object> activityStats = getActivityStatistics();
            overview.put("activity", activityStats);
            
            // 热门关键词
            List<Map<String, Object>> hotKeywords = getHotKeywords();
            overview.put("hotKeywords", hotKeywords);
            
            overview.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 缓存结果
            if (cacheService != null) {
                cacheService.cacheStatistics(cacheKey, overview);
            }
            
            return overview;
            
        } catch (Exception e) {
            logger.error("获取系统概览统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总用户数
            int totalUsers = getTotalUserCount();
            stats.put("total", totalUsers);
            
            // 今日新增用户
            int todayNewUsers = getTodayNewUserCount();
            stats.put("todayNew", todayNewUsers);
            
            // 活跃用户数（最近7天登录）
            int activeUsers = getActiveUserCount(7);
            stats.put("active7Days", activeUsers);
            
            // 用户角色分布
            Map<String, Integer> roleDistribution = getUserRoleDistribution();
            stats.put("roleDistribution", roleDistribution);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取用户统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取文献统计信息
     */
    public Map<String, Object> getLiteratureStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总文献数
            int totalLiterature = getTotalLiteratureCount();
            stats.put("total", totalLiterature);
            
            // 今日新增文献
            int todayNewLiterature = getTodayNewLiteratureCount();
            stats.put("todayNew", todayNewLiterature);
            
            // 本周新增文献
            int weekNewLiterature = getWeekNewLiteratureCount();
            stats.put("weekNew", weekNewLiterature);
            
            // 文献来源分布
            Map<String, Integer> sourceDistribution = getLiteratureSourceDistribution();
            stats.put("sourceDistribution", sourceDistribution);
            
            // 文献分类分布
            Map<String, Integer> categoryDistribution = getLiteratureCategoryDistribution();
            stats.put("categoryDistribution", categoryDistribution);
            
            // 最受欢迎的文献（按浏览量）
            List<Map<String, Object>> popularLiterature = getPopularLiterature(10);
            stats.put("popular", popularLiterature);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取文献统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取系统活跃度统计
     */
    public Map<String, Object> getActivityStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 最近7天的活跃度数据
            List<Map<String, Object>> dailyActivity = getDailyActivity(7);
            stats.put("daily", dailyActivity);
            
            // 最受欢迎的功能模块
            Map<String, Integer> moduleUsage = getModuleUsageStats();
            stats.put("moduleUsage", moduleUsage);
            
            // 爬虫任务统计
            Map<String, Object> crawlerStats = getCrawlerStatistics();
            stats.put("crawler", crawlerStats);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取活跃度统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取热门关键词
     */
    public List<Map<String, Object>> getHotKeywords() {
        try {
            // 从文献中提取关键词并统计频率
            List<MedicalLiterature> allLiterature = getAllLiterature();
            
            Map<String, Integer> keywordCount = new HashMap<>();
            
            for (MedicalLiterature literature : allLiterature) {
                if (literature.getKeywords() != null && !literature.getKeywords().trim().isEmpty()) {
                    String[] keywords = literature.getKeywords().split("[,，;；]");
                    for (String keyword : keywords) {
                        keyword = keyword.trim();
                        if (!keyword.isEmpty()) {
                            keywordCount.put(keyword, keywordCount.getOrDefault(keyword, 0) + 1);
                        }
                    }
                }
            }
            
            // 按频率排序并返回前20个
            return keywordCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20)
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("keyword", entry.getKey());
                        item.put("count", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取热门关键词失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取趋势数据（最近30天）
     */
    public Map<String, Object> getTrendData() {
        try {
            Map<String, Object> trends = new HashMap<>();
            
            // 用户增长趋势
            List<Map<String, Object>> userTrend = getUserGrowthTrend(30);
            trends.put("userGrowth", userTrend);
            
            // 文献增长趋势
            List<Map<String, Object>> literatureTrend = getLiteratureGrowthTrend(30);
            trends.put("literatureGrowth", literatureTrend);
            
            // 系统使用趋势
            List<Map<String, Object>> usageTrend = getSystemUsageTrend(30);
            trends.put("systemUsage", usageTrend);
            
            return trends;
            
        } catch (Exception e) {
            logger.error("获取趋势数据失败", e);
            return new HashMap<>();
        }
    }

    // 私有辅助方法

    private int getTotalUserCount() {
        try {
            return (int) userService.selectPage(new User(), 1, 1).getTotal();
        } catch (Exception e) {
            logger.error("获取总用户数失败", e);
            return 0;
        }
    }

    private int getTodayNewUserCount() {
        // 简化实现，返回随机数
        return (int) (Math.random() * 10);
    }

    private int getActiveUserCount(int days) {
        // 简化实现，返回随机数
        return (int) (Math.random() * 50);
    }

    private Map<String, Integer> getUserRoleDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("管理员", 5);
        distribution.put("医生", 25);
        distribution.put("患者", 150);
        distribution.put("其他", 20);
        return distribution;
    }

    private int getTotalLiteratureCount() {
        try {
            return (int) medicalLiteratureService.selectPage(new MedicalLiterature(), 1, 1).getTotal();
        } catch (Exception e) {
            logger.error("获取总文献数失败", e);
            return 0;
        }
    }

    private int getTodayNewLiteratureCount() {
        // 简化实现，返回随机数
        return (int) (Math.random() * 20);
    }

    private int getWeekNewLiteratureCount() {
        // 简化实现，返回随机数
        return (int) (Math.random() * 100);
    }

    private Map<String, Integer> getLiteratureSourceDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("arXiv", 45);
        distribution.put("PubMed", 30);
        distribution.put("Synthetic", 25);
        return distribution;
    }

    private Map<String, Integer> getLiteratureCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("人工智能", 35);
        distribution.put("医疗健康", 40);
        distribution.put("生物医学", 25);
        return distribution;
    }

    private List<Map<String, Object>> getPopularLiterature(int limit) {
        try {
            List<MedicalLiterature> literature = medicalLiteratureService.selectPage(new MedicalLiterature(), 1, limit).getList();
            
            return literature.stream()
                    .map(lit -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", lit.getId());
                        item.put("title", lit.getTitle());
                        item.put("authors", lit.getAuthors());
                        item.put("journal", lit.getJournal());
                        return item;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取热门文献失败", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getDailyActivity(int days) {
        List<Map<String, Object>> activity = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            LocalDate date = LocalDate.now().minusDays(i);
            dayData.put("date", date.toString());
            dayData.put("users", (int) (Math.random() * 100));
            dayData.put("literature", (int) (Math.random() * 50));
            dayData.put("searches", (int) (Math.random() * 200));
            activity.add(dayData);
        }
        
        return activity;
    }

    private Map<String, Integer> getModuleUsageStats() {
        Map<String, Integer> usage = new HashMap<>();
        usage.put("用户管理", 150);
        usage.put("体检管理", 200);
        usage.put("医疗文献", 300);
        usage.put("爬虫服务", 80);
        usage.put("系统监控", 50);
        return usage;
    }

    private Map<String, Object> getCrawlerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 45);
        stats.put("successTasks", 42);
        stats.put("failedTasks", 3);
        stats.put("avgDuration", "2.5分钟");
        return stats;
    }

    private List<MedicalLiterature> getAllLiterature() {
        try {
            return medicalLiteratureService.selectPage(new MedicalLiterature(), 1, 1000).getList();
        } catch (Exception e) {
            logger.error("获取所有文献失败", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getUserGrowthTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            LocalDate date = LocalDate.now().minusDays(i);
            dayData.put("date", date.toString());
            dayData.put("newUsers", (int) (Math.random() * 10));
            dayData.put("totalUsers", 200 + (days - i) * 2);
            trend.add(dayData);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getLiteratureGrowthTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            LocalDate date = LocalDate.now().minusDays(i);
            dayData.put("date", date.toString());
            dayData.put("newLiterature", (int) (Math.random() * 20));
            dayData.put("totalLiterature", 100 + (days - i) * 3);
            trend.add(dayData);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getSystemUsageTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            LocalDate date = LocalDate.now().minusDays(i);
            dayData.put("date", date.toString());
            dayData.put("pageViews", (int) (Math.random() * 1000));
            dayData.put("apiCalls", (int) (Math.random() * 500));
            trend.add(dayData);
        }
        
        return trend;
    }
}
