package com.example.crawler.controller;

import com.example.common.Result;
import com.example.crawler.core.Orchestrator;
import com.example.crawler.core.model.CrawlRequest;
import com.example.crawler.core.model.CrawlResult;
import com.example.crawler.pipeline.MetricsAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Map;

/**
 * 简化版爬虫控制器 - V2
 */
@RestController
@RequestMapping("/api/crawler/v2")
public class CrawlerV2Controller {

    @Autowired
    private Orchestrator orchestrator;

    @Autowired
    private MetricsAdapter metricsAdapter;
    
    /**
     * 简化的爬虫接口
     * GET /api/crawler/v2/crawl?keyword=cancer&maxResults=10&source=arxiv
     */
    @GetMapping("/crawl")
    public Result crawl(@RequestParam String keyword,
                       @RequestParam(defaultValue = "10") int maxResults,
                       @RequestParam(required = false) String source,
                       @RequestParam(defaultValue = "false") boolean classify) {
        
        CrawlRequest request = new CrawlRequest(keyword, maxResults);
        request.setClassifyEnabled(classify);
        
        // 如果指定了数据源，只使用该数据源
        if (source != null && !source.trim().isEmpty()) {
            request.setSources(Arrays.asList(source.trim()));
        }
        
        CrawlResult result = orchestrator.crawl(request);
        
        return Result.success(result);
    }
    
    /**
     * POST方式的爬虫接口（支持更复杂的参数）
     * POST /api/crawler/v2/crawl
     * {
     *   "keyword": "cancer",
     *   "maxResults": 20,
     *   "sources": ["arxiv", "pubmed"],
     *   "classifyEnabled": true
     * }
     *
     * 【未使用接口】前端使用GET方式调用，此POST接口保留备用
     */
    // @PostMapping("/crawl")
    // public Result crawlPost(@RequestBody CrawlRequest request) {
    //     CrawlResult result = orchestrator.crawl(request);
    //     return Result.success(result);
    // }
    
    /**
     * 获取爬虫状态
     * GET /api/crawler/v2/status
     */
    @GetMapping("/status")
    public Result getStatus() {
        Map<String, Object> status = orchestrator.getStatus();
        return Result.success(status);
    }
    
    /**
     * 获取监控指标
     * GET /api/crawler/v2/metrics
     *
     * 【未使用接口】前端未调用此接口，保留用于监控
     */
    // @GetMapping("/metrics")
    // public Result getMetrics() {
    //     Object metrics = metricsAdapter.getMetrics();
    //     return Result.success(metrics);
    // }

    /**
     * 健康检查
     * GET /api/crawler/v2/health
     *
     * 【未使用接口】前端未调用此接口，保留用于健康检查
     */
    // @GetMapping("/health")
    // public Result health() {
    //     Map<String, Object> status = orchestrator.getStatus();
    //     boolean healthy = (Boolean) status.get("enabled");
    //
    //     if (healthy) {
    //         return Result.success("Crawler is healthy");
    //     } else {
    //         return Result.error("Crawler is disabled");
    //     }
    // }
}
