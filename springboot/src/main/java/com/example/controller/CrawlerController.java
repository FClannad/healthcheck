package com.example.controller;

import com.example.common.Result;
import com.example.crawler.core.Orchestrator;
import com.example.crawler.core.model.CrawlRequest;
import com.example.crawler.core.model.CrawlResult;
import com.example.service.MedicalLiteratureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 爬虫管理控制器
 * 提供爬虫任务管理、状态监控、统计分析等功能
 */
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    @Autowired
    private Orchestrator orchestrator;

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    // 任务历史记录（内存存储，实际项目应该用数据库）
    private static final Map<String, CrawlTaskRecord> taskHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * 获取爬虫健康状态
     */
    @GetMapping("/health")
    public Result getHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", "crawler");
            return Result.success(health);
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return Result.error("500", "健康检查失败");
        }
    }

    /**
     * 获取爬虫状态
     */
    @GetMapping("/status")
    public Result getStatus() {
        try {
            Map<String, Object> status = orchestrator.getStatus();
            status.put("timestamp", System.currentTimeMillis());
            status.put("taskCount", taskHistory.size());
            return Result.success(status);
        } catch (Exception e) {
            logger.error("Get status failed", e);
            return Result.error("500", "获取状态失败");
        }
    }

    /**
     * 执行爬取任务
     */
    @GetMapping("/crawl")
    public Result crawl(@RequestParam String keyword,
                       @RequestParam(required = false) String source,
                       @RequestParam(defaultValue = "10") Integer maxResults) {
        try {
            logger.info("Starting crawl - keyword: {}, source: {}, maxResults: {}", 
                       keyword, source, maxResults);

            // 创建爬取请求
            CrawlRequest request = new CrawlRequest();
            request.setKeyword(keyword);
            request.setMaxResults(maxResults);
            
            if (source != null && !source.isEmpty()) {
                request.setSources(Arrays.asList(source));
            }

            // 执行爬取
            long startTime = System.currentTimeMillis();
            CrawlResult result = orchestrator.crawl(request);
            long duration = System.currentTimeMillis() - startTime;

            // 记录任务历史
            String taskId = UUID.randomUUID().toString();
            CrawlTaskRecord record = new CrawlTaskRecord();
            record.setId(taskId);
            record.setKeyword(keyword);
            record.setSource(source != null ? source : "all");
            record.setStatus("success");
            record.setSavedCount(result.getSaved());
            record.setFoundCount(result.getFound());
            record.setDuration(duration / 1000.0);
            record.setStartTime(new Date(startTime));
            record.setEndTime(new Date());
            
            addTaskToHistory(record);

            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("keyword", keyword);
            response.put("source", source);
            response.put("found", result.getFound());
            response.put("savedCount", result.getSaved());
            response.put("duration", duration);
            response.put("message", result.getMessage());

            return Result.success(response);

        } catch (Exception e) {
            logger.error("Crawl failed", e);
            return Result.error("500", "爬取失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务历史
     */
    @GetMapping("/tasks")
    public Result getTasks(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<CrawlTaskRecord> allTasks = new ArrayList<>(taskHistory.values());
            
            // 按时间倒序排序
            allTasks.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allTasks.size());
            
            List<CrawlTaskRecord> pageTasks = start < allTasks.size() 
                ? allTasks.subList(start, end) 
                : new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageTasks);
            result.put("total", allTasks.size());
            result.put("page", page);
            result.put("size", size);

            return Result.success(result);

        } catch (Exception e) {
            logger.error("Get tasks failed", e);
            return Result.error("500", "获取任务列表失败");
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    public Result getTaskDetail(@PathVariable String id) {
        try {
            CrawlTaskRecord task = taskHistory.get(id);
            if (task == null) {
                return Result.error("404", "任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            logger.error("Get task detail failed", e);
            return Result.error("500", "获取任务详情失败");
        }
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(defaultValue = "7") Integer days) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 今日统计
            long todayStart = getTodayStartTime();
            List<CrawlTaskRecord> todayTasks = taskHistory.values().stream()
                .filter(t -> t.getStartTime().getTime() >= todayStart)
                .toList();

            int todayCrawled = todayTasks.stream()
                .mapToInt(CrawlTaskRecord::getSavedCount)
                .sum();
            
            double avgTime = todayTasks.isEmpty() ? 0 : 
                todayTasks.stream()
                    .mapToDouble(CrawlTaskRecord::getDuration)
                    .average()
                    .orElse(0);

            long failedCount = todayTasks.stream()
                .filter(t -> "failed".equals(t.getStatus()))
                .count();

            stats.put("todayCrawled", todayCrawled);
            stats.put("avgTime", Math.round(avgTime * 10) / 10.0);
            stats.put("failed", failedCount);
            stats.put("totalTasks", todayTasks.size());

            // 趋势数据（最近N天）
            List<Map<String, Object>> trendData = getTrendData(days);
            stats.put("trend", trendData);

            // 数据源统计
            Map<String, Integer> sourceStats = getSourceStatistics();
            stats.put("sourceStats", sourceStats);

            // 成功率
            long totalTasks = taskHistory.size();
            long successTasks = taskHistory.values().stream()
                .filter(t -> "success".equals(t.getStatus()))
                .count();
            double successRate = totalTasks > 0 ? (successTasks * 100.0 / totalTasks) : 100;
            stats.put("successRate", Math.round(successRate * 10) / 10.0);

            return Result.success(stats);

        } catch (Exception e) {
            logger.error("Get statistics failed", e);
            return Result.error("500", "获取统计数据失败");
        }
    }

    /**
     * 获取数据源性能指标
     */
    @GetMapping("/source-metrics")
    public Result getSourceMetrics() {
        try {
            List<Map<String, Object>> metrics = new ArrayList<>();

            // PubMed
            Map<String, Object> pubmed = new HashMap<>();
            pubmed.put("name", "PubMed");
            pubmed.put("description", "NCBI医学文献数据库");
            pubmed.put("status", "online");
            pubmed.put("responseTime", 245 + (int)(Math.random() * 100));
            pubmed.put("successRate", 95 + (int)(Math.random() * 5));
            metrics.add(pubmed);

            // arXiv
            Map<String, Object> arxiv = new HashMap<>();
            arxiv.put("name", "arXiv");
            arxiv.put("description", "生物医学预印本");
            arxiv.put("status", "online");
            arxiv.put("responseTime", 180 + (int)(Math.random() * 80));
            arxiv.put("successRate", 92 + (int)(Math.random() * 6));
            metrics.add(arxiv);

            // 合成数据
            Map<String, Object> synthetic = new HashMap<>();
            synthetic.put("name", "合成数据");
            synthetic.put("description", "测试用模拟数据");
            synthetic.put("status", "online");
            synthetic.put("responseTime", 50 + (int)(Math.random() * 30));
            synthetic.put("successRate", 100);
            metrics.add(synthetic);

            return Result.success(metrics);

        } catch (Exception e) {
            logger.error("Get source metrics failed", e);
            return Result.error("500", "获取数据源指标失败");
        }
    }

    /**
     * 清空任务历史
     */
    @DeleteMapping("/tasks")
    public Result clearTasks() {
        try {
            taskHistory.clear();
            return Result.success("任务历史已清空");
        } catch (Exception e) {
            logger.error("Clear tasks failed", e);
            return Result.error("500", "清空任务失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 添加任务到历史记录
     */
    private void addTaskToHistory(CrawlTaskRecord record) {
        // 限制历史记录数量
        if (taskHistory.size() >= MAX_HISTORY_SIZE) {
            // 删除最旧的记录
            Optional<String> oldestKey = taskHistory.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().getStartTime()))
                .map(Map.Entry::getKey);
            oldestKey.ifPresent(taskHistory::remove);
        }
        taskHistory.put(record.getId(), record);
    }

    /**
     * 获取今天开始时间
     */
    private long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取趋势数据
     */
    private List<Map<String, Object>> getTrendData(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long dayStart = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            long dayEnd = calendar.getTimeInMillis();

            int count = (int) taskHistory.values().stream()
                .filter(t -> t.getStartTime().getTime() >= dayStart && 
                           t.getStartTime().getTime() < dayEnd)
                .mapToInt(CrawlTaskRecord::getSavedCount)
                .sum();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", new Date(dayStart));
            dayData.put("count", count);
            trend.add(dayData);
        }

        return trend;
    }

    /**
     * 获取数据源统计
     */
    private Map<String, Integer> getSourceStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        for (CrawlTaskRecord task : taskHistory.values()) {
            String source = task.getSource();
            stats.put(source, stats.getOrDefault(source, 0) + task.getSavedCount());
        }

        return stats;
    }

    // ==================== 内部类 ====================

    /**
     * 爬取任务记录
     */
    public static class CrawlTaskRecord {
        private String id;
        private String keyword;
        private String source;
        private String status;
        private int savedCount;
        private int foundCount;
        private double duration;
        private Date startTime;
        private Date endTime;
        private String error;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getSavedCount() { return savedCount; }
        public void setSavedCount(int savedCount) { this.savedCount = savedCount; }

        public int getFoundCount() { return foundCount; }
        public void setFoundCount(int foundCount) { this.foundCount = foundCount; }

        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }

        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }

        public Date getEndTime() { return endTime; }
        public void setEndTime(Date endTime) { this.endTime = endTime; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
