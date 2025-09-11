package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 爬虫性能监控服务
 * 跟踪和分析爬虫性能指标
 */
@Service
public class CrawlerMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerMetricsService.class);

    // 性能指标
    private final AtomicInteger totalCrawlRequests = new AtomicInteger(0);
    private final AtomicInteger successfulCrawls = new AtomicInteger(0);
    private final AtomicInteger failedCrawls = new AtomicInteger(0);
    private final AtomicLong totalCrawlTime = new AtomicLong(0);
    private final AtomicInteger totalPapersFound = new AtomicInteger(0);
    private final AtomicInteger totalPapersSaved = new AtomicInteger(0);

    // 数据源性能指标
    private final Map<String, SourceMetrics> sourceMetrics = new ConcurrentHashMap<>();
    
    // 最近的爬虫任务记录
    private final List<CrawlRecord> recentCrawls = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_RECENT_RECORDS = 100;

    /**
     * 数据源性能指标类
     */
    public static class SourceMetrics {
        private final AtomicInteger requests = new AtomicInteger(0);
        private final AtomicInteger successes = new AtomicInteger(0);
        private final AtomicInteger failures = new AtomicInteger(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicInteger papersFound = new AtomicInteger(0);

        public int getRequests() { return requests.get(); }
        public int getSuccesses() { return successes.get(); }
        public int getFailures() { return failures.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public int getPapersFound() { return papersFound.get(); }
        
        public double getSuccessRate() {
            int total = requests.get();
            return total > 0 ? (double) successes.get() / total * 100 : 0;
        }
        
        public double getAverageTime() {
            int total = requests.get();
            return total > 0 ? (double) totalTime.get() / total : 0;
        }
    }

    /**
     * 爬虫记录类
     */
    public static class CrawlRecord {
        private final String keyword;
        private final String source;
        private final long startTime;
        private final long endTime;
        private final boolean success;
        private final int papersFound;
        private final int papersSaved;
        private final String errorMessage;

        public CrawlRecord(String keyword, String source, long startTime, long endTime, 
                          boolean success, int papersFound, int papersSaved, String errorMessage) {
            this.keyword = keyword;
            this.source = source;
            this.startTime = startTime;
            this.endTime = endTime;
            this.success = success;
            this.papersFound = papersFound;
            this.papersSaved = papersSaved;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getKeyword() { return keyword; }
        public String getSource() { return source; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public long getDuration() { return endTime - startTime; }
        public boolean isSuccess() { return success; }
        public int getPapersFound() { return papersFound; }
        public int getPapersSaved() { return papersSaved; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 记录爬虫开始
     */
    public long recordCrawlStart(String keyword, String source) {
        totalCrawlRequests.incrementAndGet();
        getSourceMetrics(source).requests.incrementAndGet();
        
        long startTime = System.currentTimeMillis();
        logger.debug("爬虫开始 - 关键词: {}, 数据源: {}", keyword, source);
        
        return startTime;
    }

    /**
     * 记录爬虫成功
     */
    public void recordCrawlSuccess(String keyword, String source, long startTime, 
                                  int papersFound, int papersSaved) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        successfulCrawls.incrementAndGet();
        totalCrawlTime.addAndGet(duration);
        totalPapersFound.addAndGet(papersFound);
        totalPapersSaved.addAndGet(papersSaved);
        
        SourceMetrics metrics = getSourceMetrics(source);
        metrics.successes.incrementAndGet();
        metrics.totalTime.addAndGet(duration);
        metrics.papersFound.addAndGet(papersFound);
        
        // 记录爬虫任务
        addCrawlRecord(new CrawlRecord(keyword, source, startTime, endTime, 
                                     true, papersFound, papersSaved, null));
        
        logger.info("爬虫成功 - 关键词: {}, 数据源: {}, 耗时: {}ms, 找到: {}, 保存: {}", 
                   keyword, source, duration, papersFound, papersSaved);
    }

    /**
     * 记录爬虫失败
     */
    public void recordCrawlFailure(String keyword, String source, long startTime, String errorMessage) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        failedCrawls.incrementAndGet();
        totalCrawlTime.addAndGet(duration);
        
        SourceMetrics metrics = getSourceMetrics(source);
        metrics.failures.incrementAndGet();
        metrics.totalTime.addAndGet(duration);
        
        // 记录爬虫任务
        addCrawlRecord(new CrawlRecord(keyword, source, startTime, endTime, 
                                     false, 0, 0, errorMessage));
        
        logger.warn("爬虫失败 - 关键词: {}, 数据源: {}, 耗时: {}ms, 错误: {}", 
                   keyword, source, duration, errorMessage);
    }

    /**
     * 获取数据源指标
     */
    private SourceMetrics getSourceMetrics(String source) {
        return sourceMetrics.computeIfAbsent(source, k -> new SourceMetrics());
    }

    /**
     * 添加爬虫记录
     */
    private void addCrawlRecord(CrawlRecord record) {
        synchronized (recentCrawls) {
            recentCrawls.add(record);
            if (recentCrawls.size() > MAX_RECENT_RECORDS) {
                recentCrawls.remove(0);
            }
        }
    }

    /**
     * 获取总体性能指标
     */
    public Map<String, Object> getOverallMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        int totalRequests = totalCrawlRequests.get();
        int successful = successfulCrawls.get();
        int failed = failedCrawls.get();
        
        metrics.put("totalRequests", totalRequests);
        metrics.put("successfulCrawls", successful);
        metrics.put("failedCrawls", failed);
        metrics.put("successRate", totalRequests > 0 ? (double) successful / totalRequests * 100 : 0);
        metrics.put("averageCrawlTime", totalRequests > 0 ? (double) totalCrawlTime.get() / totalRequests : 0);
        metrics.put("totalPapersFound", totalPapersFound.get());
        metrics.put("totalPapersSaved", totalPapersSaved.get());
        metrics.put("saveRate", totalPapersFound.get() > 0 ? 
                   (double) totalPapersSaved.get() / totalPapersFound.get() * 100 : 0);
        
        return metrics;
    }

    /**
     * 获取数据源性能指标
     */
    public Map<String, Map<String, Object>> getSourceMetrics() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        for (Map.Entry<String, SourceMetrics> entry : sourceMetrics.entrySet()) {
            String source = entry.getKey();
            SourceMetrics metrics = entry.getValue();
            
            Map<String, Object> sourceData = new HashMap<>();
            sourceData.put("requests", metrics.getRequests());
            sourceData.put("successes", metrics.getSuccesses());
            sourceData.put("failures", metrics.getFailures());
            sourceData.put("successRate", metrics.getSuccessRate());
            sourceData.put("averageTime", metrics.getAverageTime());
            sourceData.put("papersFound", metrics.getPapersFound());
            
            result.put(source, sourceData);
        }
        
        return result;
    }

    /**
     * 获取最近的爬虫记录
     */
    public List<CrawlRecord> getRecentCrawls(int limit) {
        synchronized (recentCrawls) {
            int size = recentCrawls.size();
            int fromIndex = Math.max(0, size - limit);
            return new ArrayList<>(recentCrawls.subList(fromIndex, size));
        }
    }

    /**
     * 获取性能趋势数据
     */
    public Map<String, Object> getPerformanceTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        synchronized (recentCrawls) {
            if (recentCrawls.isEmpty()) {
                return trends;
            }
            
            // 按小时统计
            Map<String, Integer> hourlySuccess = new HashMap<>();
            Map<String, Integer> hourlyFailure = new HashMap<>();
            Map<String, Integer> hourlyPapers = new HashMap<>();
            
            Calendar cal = Calendar.getInstance();
            for (CrawlRecord record : recentCrawls) {
                cal.setTimeInMillis(record.getStartTime());
                String hour = String.format("%02d:00", cal.get(Calendar.HOUR_OF_DAY));
                
                if (record.isSuccess()) {
                    hourlySuccess.put(hour, hourlySuccess.getOrDefault(hour, 0) + 1);
                    hourlyPapers.put(hour, hourlyPapers.getOrDefault(hour, 0) + record.getPapersSaved());
                } else {
                    hourlyFailure.put(hour, hourlyFailure.getOrDefault(hour, 0) + 1);
                }
            }
            
            trends.put("hourlySuccess", hourlySuccess);
            trends.put("hourlyFailure", hourlyFailure);
            trends.put("hourlyPapers", hourlyPapers);
        }
        
        return trends;
    }

    /**
     * 重置所有指标
     */
    public void resetMetrics() {
        totalCrawlRequests.set(0);
        successfulCrawls.set(0);
        failedCrawls.set(0);
        totalCrawlTime.set(0);
        totalPapersFound.set(0);
        totalPapersSaved.set(0);
        
        sourceMetrics.clear();
        
        synchronized (recentCrawls) {
            recentCrawls.clear();
        }
        
        logger.info("爬虫性能指标已重置");
    }

    /**
     * 获取健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        int totalRequests = totalCrawlRequests.get();
        double successRate = totalRequests > 0 ? 
            (double) successfulCrawls.get() / totalRequests * 100 : 100;
        
        String status = "UP";
        if (successRate < 50) {
            status = "DOWN";
        } else if (successRate < 80) {
            status = "DEGRADED";
        }
        
        health.put("status", status);
        health.put("successRate", successRate);
        health.put("totalRequests", totalRequests);
        health.put("recentCrawls", recentCrawls.size());
        
        return health;
    }
}
