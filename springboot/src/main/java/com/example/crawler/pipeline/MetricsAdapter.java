package com.example.crawler.pipeline;

import com.example.crawler.core.model.CrawlResult;
import com.example.service.CrawlerMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监控指标适配器
 * 将新爬虫系统的结果适配到现有的监控服务
 */
@Component
public class MetricsAdapter {
    
    @Autowired
    private CrawlerMetricsService metricsService;
    
    /**
     * 记录爬虫结果
     */
    public void recordCrawlResult(CrawlResult result) {
        long startTime = System.currentTimeMillis() - result.getDurationMs();

        if (result.getSaved() > 0) {
            metricsService.recordCrawlSuccess(
                result.getKeyword(),
                "multi-source", // 新系统使用多数据源
                startTime,
                result.getFound(),
                result.getSaved()
            );
        } else {
            metricsService.recordCrawlFailure(
                result.getKeyword(),
                "multi-source",
                startTime,
                result.getMessage()
            );
        }
    }
    
    /**
     * 获取监控指标
     */
    public Object getMetrics() {
        return metricsService.getOverallMetrics();
    }
}
