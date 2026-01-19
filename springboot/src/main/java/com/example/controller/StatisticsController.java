package com.example.controller;

import com.example.common.Result;
import com.example.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据控制器
 * 为前端数据可视化提供API接口
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取仪表板数据
     */
    @GetMapping("/dashboard")
    public Result getDashboardData() {
        try {
            logger.info("获取仪表板统计数据");
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // 系统概览
            Map<String, Object> overview = statisticsService.getSystemOverview();
            dashboard.put("overview", overview);
            
            // 实时统计卡片数据
            Map<String, Object> cards = new HashMap<>();
            cards.put("totalUsers", overview.getOrDefault("users", new HashMap<>()));
            cards.put("totalLiterature", overview.getOrDefault("literature", new HashMap<>()));
            cards.put("systemActivity", overview.getOrDefault("activity", new HashMap<>()));
            dashboard.put("cards", cards);
            
            // 热门关键词云
            dashboard.put("keywordCloud", overview.get("hotKeywords"));
            
            return Result.success(dashboard);
            
        } catch (Exception e) {
            logger.error("获取仪表板数据失败", e);
            return Result.error("500", "获取仪表板数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/users")
    public Result getUserStatistics() {
        try {
            logger.info("获取用户统计数据");
            
            Map<String, Object> userStats = statisticsService.getUserStatistics();
            
            return Result.success(userStats);
            
        } catch (Exception e) {
            logger.error("获取用户统计数据失败", e);
            return Result.error("500", "获取用户统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取文献统计数据
     */
    @GetMapping("/literature")
    public Result getLiteratureStatistics() {
        try {
            logger.info("获取文献统计数据");
            
            Map<String, Object> literatureStats = statisticsService.getLiteratureStatistics();
            
            return Result.success(literatureStats);
            
        } catch (Exception e) {
            logger.error("获取文献统计数据失败", e);
            return Result.error("500", "获取文献统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trends")
    public Result getTrendData(@RequestParam(defaultValue = "30") int days) {
        try {
            logger.info("获取趋势数据，天数: {}", days);
            
            if (days < 1 || days > 365) {
                return Result.error("400", "天数参数必须在1-365之间");
            }
            
            Map<String, Object> trends = statisticsService.getTrendData();
            
            return Result.success(trends);
            
        } catch (Exception e) {
            logger.error("获取趋势数据失败", e);
            return Result.error("500", "获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门关键词
     */
    @GetMapping("/keywords")
    public Result getHotKeywords(@RequestParam(defaultValue = "20") int limit) {
        try {
            logger.info("获取热门关键词，限制数量: {}", limit);
            
            if (limit < 1 || limit > 100) {
                return Result.error("400", "限制数量必须在1-100之间");
            }
            
            return Result.success(statisticsService.getHotKeywords());
            
        } catch (Exception e) {
            logger.error("获取热门关键词失败", e);
            return Result.error("500", "获取热门关键词失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    public Result getRealtimeData() {
        try {
            logger.info("获取实时统计数据");
            
            Map<String, Object> realtime = new HashMap<>();
            
            // 当前在线用户数（模拟数据）
            realtime.put("onlineUsers", (int) (Math.random() * 50) + 10);
            
            // 今日访问量
            realtime.put("todayVisits", (int) (Math.random() * 1000) + 500);
            
            // 今日新增文献
            realtime.put("todayNewLiterature", (int) (Math.random() * 20) + 5);
            
            // 系统负载（模拟数据）
            Map<String, Object> systemLoad = new HashMap<>();
            systemLoad.put("cpu", Math.round(Math.random() * 100 * 100.0) / 100.0);
            systemLoad.put("memory", Math.round(Math.random() * 100 * 100.0) / 100.0);
            systemLoad.put("disk", Math.round(Math.random() * 100 * 100.0) / 100.0);
            realtime.put("systemLoad", systemLoad);
            
            // 最近活动
            realtime.put("recentActivities", getRecentActivities());
            
            return Result.success(realtime);
            
        } catch (Exception e) {
            logger.error("获取实时统计数据失败", e);
            return Result.error("500", "获取实时统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据导出
     */
    @GetMapping("/export")
    public Result exportStatistics(@RequestParam String type, @RequestParam(required = false) String format) {
        try {
            logger.info("导出统计数据，类型: {}, 格式: {}", type, format);
            
            if (format == null) {
                format = "json";
            }
            
            Map<String, Object> exportData = new HashMap<>();
            
            switch (type) {
                case "overview":
                    exportData = statisticsService.getSystemOverview();
                    break;
                case "users":
                    exportData = statisticsService.getUserStatistics();
                    break;
                case "literature":
                    exportData = statisticsService.getLiteratureStatistics();
                    break;
                case "trends":
                    exportData = statisticsService.getTrendData();
                    break;
                default:
                    return Result.error("400", "不支持的导出类型: " + type);
            }
            
            // 添加导出元数据
            Map<String, Object> result = new HashMap<>();
            result.put("type", type);
            result.put("format", format);
            result.put("exportTime", System.currentTimeMillis());
            result.put("data", exportData);
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("导出统计数据失败", e);
            return Result.error("500", "导出统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取自定义统计查询
     */
    @PostMapping("/custom")
    public Result getCustomStatistics(@RequestBody Map<String, Object> query) {
        try {
            logger.info("执行自定义统计查询: {}", query);
            
            String queryType = (String) query.get("type");
            Map<String, Object> params = (Map<String, Object>) query.getOrDefault("params", new HashMap<>());
            
            Map<String, Object> result = new HashMap<>();
            
            switch (queryType) {
                case "keyword_analysis":
                    String keyword = (String) params.get("keyword");
                    result = analyzeKeyword(keyword);
                    break;
                    
                case "time_range":
                    String startDate = (String) params.get("startDate");
                    String endDate = (String) params.get("endDate");
                    result = getTimeRangeStatistics(startDate, endDate);
                    break;
                    
                case "source_comparison":
                    result = getSourceComparison();
                    break;
                    
                default:
                    return Result.error("400", "不支持的查询类型: " + queryType);
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("执行自定义统计查询失败", e);
            return Result.error("500", "执行自定义统计查询失败: " + e.getMessage());
        }
    }

    // 私有辅助方法

    private Object getRecentActivities() {
        // 模拟最近活动数据
        return new String[]{
            "用户张三登录系统",
            "新增文献《AI在医疗中的应用》",
            "完成定时爬虫任务",
            "用户李四下载文献",
            "系统健康检查完成"
        };
    }

    private Map<String, Object> analyzeKeyword(String keyword) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("keyword", keyword);
        analysis.put("relatedLiterature", (int) (Math.random() * 50));
        analysis.put("searchFrequency", (int) (Math.random() * 100));
        analysis.put("trendScore", Math.round(Math.random() * 100 * 100.0) / 100.0);
        return analysis;
    }

    private Map<String, Object> getTimeRangeStatistics(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("newUsers", (int) (Math.random() * 100));
        stats.put("newLiterature", (int) (Math.random() * 200));
        stats.put("totalVisits", (int) (Math.random() * 5000));
        return stats;
    }

    private Map<String, Object> getSourceComparison() {
        Map<String, Object> comparison = new HashMap<>();
        
        Map<String, Object> arxiv = new HashMap<>();
        arxiv.put("count", 45);
        arxiv.put("quality", 8.5);
        arxiv.put("avgCitations", 12);
        
        Map<String, Object> pubmed = new HashMap<>();
        pubmed.put("count", 30);
        pubmed.put("quality", 9.2);
        pubmed.put("avgCitations", 18);
        
        Map<String, Object> synthetic = new HashMap<>();
        synthetic.put("count", 25);
        synthetic.put("quality", 7.0);
        synthetic.put("avgCitations", 5);
        
        comparison.put("arXiv", arxiv);
        comparison.put("PubMed", pubmed);
        comparison.put("Synthetic", synthetic);
        
        return comparison;
    }
}
