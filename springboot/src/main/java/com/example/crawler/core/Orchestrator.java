package com.example.crawler.core;

import com.example.crawler.core.model.CrawlRequest;
import com.example.crawler.core.model.CrawlResult;
import com.example.crawler.pipeline.Deduplicator;
import com.example.crawler.pipeline.MetricsAdapter;
import com.example.crawler.pipeline.Normalizer;
import com.example.crawler.sources.SourceClient;
import com.example.entity.MedicalLiterature;
import com.example.service.LiteratureClassifierService;
import com.example.service.MedicalLiteratureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.*;

/**
 * 爬虫核心编排器 - 简化的4步流程
 */
@Service
public class Orchestrator {
    
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);
    
    @Autowired
    private CrawlerProperties crawlerProperties;

    @Autowired
    private List<SourceClient> sourceClients;

    @Autowired
    private Normalizer normalizer;

    @Autowired
    private Deduplicator deduplicator;

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    @Autowired
    private LiteratureClassifierService classifierService;

    @Autowired
    private MetricsAdapter metricsAdapter;

    // 线程池用于并发爬取
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    /**
     * 执行爬虫任务
     */
    public CrawlResult crawl(CrawlRequest request) {
        if (!crawlerProperties.isEnabled()) {
            return new CrawlResult(request.getKeyword(), 0, 0, 0);
        }
        
        long startTime = System.currentTimeMillis();
        log.info("Starting crawl for keyword: {}, maxResults: {}", request.getKeyword(), request.getMaxResults());
        
        try {
            // 第1步：拉取数据
            List<MedicalLiterature> allPapers = fetchFromSources(request);
            
            // 第2步：归一化
            normalizer.normalize(allPapers);
            
            // 第3步：去重
            List<MedicalLiterature> uniquePapers = deduplicator.deduplicate(allPapers);
            
            // 第4步：分类（可选）
            if (request.isClassifyEnabled() && crawlerProperties.isClassifyEnabled()) {
                classifierService.classifyLiteratures(uniquePapers);
            }
            
            // 第5步：保存
            int savedCount = persistPapers(uniquePapers);
            
            long duration = System.currentTimeMillis() - startTime;
            
            CrawlResult result = new CrawlResult(request.getKeyword(), allPapers.size(), savedCount, duration);
            result.setMessage(String.format("爬取完成，找到 %d 篇文献，保存 %d 篇", allPapers.size(), savedCount));

            // 记录监控指标
            metricsAdapter.recordCrawlResult(result);

            log.info("Crawl completed: found={}, saved={}, duration={}ms", allPapers.size(), savedCount, duration);
            return result;
            
        } catch (Exception e) {
            log.error("Crawl failed for keyword: {}", request.getKeyword(), e);
            long duration = System.currentTimeMillis() - startTime;
            CrawlResult result = new CrawlResult(request.getKeyword(), 0, 0, duration);
            result.setMessage("爬取失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 从数据源拉取数据（支持并发处理）
     */
    private List<MedicalLiterature> fetchFromSources(CrawlRequest request) {
        List<String> enabledSources = getEnabledSources(request);

        // 如果启用并行处理且有多个数据源
        if (crawlerProperties.isParallel() && enabledSources.size() > 1) {
            return fetchFromSourcesParallel(request, enabledSources);
        } else {
            return fetchFromSourcesSequential(request, enabledSources);
        }
    }

    /**
     * 并发从多个数据源拉取数据
     */
    private List<MedicalLiterature> fetchFromSourcesParallel(CrawlRequest request, List<String> enabledSources) {
        List<MedicalLiterature> allPapers = new ArrayList<>();
        List<CompletableFuture<List<MedicalLiterature>>> futures = new ArrayList<>();

        int maxPerSource = Math.max(1, request.getMaxResults() / enabledSources.size());

        for (String sourceName : enabledSources) {
            CompletableFuture<List<MedicalLiterature>> future = CompletableFuture.supplyAsync(() -> {
                SourceClient client = findSourceClient(sourceName);
                if (client == null) {
                    log.warn("Source {} client not found", sourceName);
                    return new ArrayList<>();
                }

                try {
                    log.info("Parallel fetching from {}: maxResults={}", sourceName, maxPerSource);
                    List<MedicalLiterature> papers = client.fetch(request.getKeyword(), maxPerSource);

                    if (papers != null && !papers.isEmpty()) {
                        log.info("Successfully fetched {} papers from {} (parallel)", papers.size(), sourceName);
                        return papers;
                    } else {
                        log.warn("No papers returned from {} (parallel)", sourceName);
                        return new ArrayList<>();
                    }
                } catch (Exception e) {
                    log.error("Error fetching from {} (parallel): {}", sourceName, e.getMessage());
                    return new ArrayList<>();
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有任务完成
        try {
            for (CompletableFuture<List<MedicalLiterature>> future : futures) {
                List<MedicalLiterature> papers = future.get(30, TimeUnit.SECONDS);
                allPapers.addAll(papers);
            }
        } catch (Exception e) {
            log.error("Error in parallel fetching: {}", e.getMessage());
        }

        return allPapers;
    }

    /**
     * 顺序从数据源拉取数据（原有逻辑）
     */
    private List<MedicalLiterature> fetchFromSourcesSequential(CrawlRequest request, List<String> enabledSources) {
        List<MedicalLiterature> allPapers = new ArrayList<>();
        Map<String, Integer> sourceStats = new HashMap<>();
        int remainingCount = request.getMaxResults();

        for (String sourceName : enabledSources) {
            if (remainingCount <= 0) break;

            SourceClient client = findSourceClient(sourceName);
            if (client == null) {
                log.warn("Source {} client not found", sourceName);
                continue;
            }

            try {
                int maxForThisSource = Math.min(remainingCount, crawlerProperties.getMaxPerSource());
                log.info("Sequential fetching from {}: maxResults={}, remaining={}", sourceName, maxForThisSource, remainingCount);

                List<MedicalLiterature> papers = client.fetch(request.getKeyword(), maxForThisSource);
                if (papers != null && !papers.isEmpty()) {
                    allPapers.addAll(papers);
                    sourceStats.put(sourceName, papers.size());
                    remainingCount -= papers.size();
                    log.info("Successfully fetched {} papers from {}", papers.size(), sourceName);
                } else {
                    log.warn("No papers returned from {}", sourceName);
                    sourceStats.put(sourceName, 0);
                }

                // 请求间隔
                if (remainingCount > 0) {
                    Thread.sleep(crawlerProperties.getRetryDelayMs());
                }

            } catch (Exception e) {
                log.error("Failed to fetch from source: {}", sourceName, e);
                sourceStats.put(sourceName, 0);
            }
        }
        
        log.info("Total fetched: {} papers from {} sources", allPapers.size(), sourceStats.size());
        return allPapers;
    }
    
    /**
     * 获取启用的数据源列表
     */
    private List<String> getEnabledSources(CrawlRequest request) {
        if (request.getSources() != null && !request.getSources().isEmpty()) {
            return request.getSources();
        }
        
        List<String> enabledSources = new ArrayList<>();
        for (String source : crawlerProperties.getSources()) {
            CrawlerProperties.SourceConfig config = crawlerProperties.getSourceConfigs().get(source);
            if (config != null && config.isEnabled()) {
                enabledSources.add(source);
            }
        }
        
        return enabledSources;
    }
    
    /**
     * 查找数据源客户端
     */
    private SourceClient findSourceClient(String sourceName) {
        return sourceClients.stream()
            .filter(client -> sourceName.equals(client.getSourceName()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 保存文献
     */
    private int persistPapers(List<MedicalLiterature> papers) {
        int savedCount = 0;

        log.info("Attempting to save {} papers to database", papers.size());

        for (int i = 0; i < papers.size(); i++) {
            MedicalLiterature paper = papers.get(i);
            try {
                log.debug("Saving paper {}/{}: {}", i + 1, papers.size(), paper.getTitle());
                medicalLiteratureService.add(paper);
                savedCount++;
                log.debug("Successfully saved paper: {}", paper.getTitle());
            } catch (Exception e) {
                log.error("Failed to save paper {}/{}: {}", i + 1, papers.size(), paper.getTitle(), e);
            }
        }

        log.info("Successfully saved {}/{} papers to database", savedCount, papers.size());
        return savedCount;
    }
    
    /**
     * 便捷的爬取方法（兼容旧接口）
     */
    public CrawlResult crawl(String keyword, int maxResults) {
        CrawlRequest request = new CrawlRequest();
        request.setKeyword(keyword);
        request.setMaxResults(maxResults);
        request.setClassifyEnabled(crawlerProperties.isClassifyEnabled());
        return crawl(request);
    }

    /**
     * 获取爬虫状态
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", crawlerProperties.isEnabled());
        status.put("sources", crawlerProperties.getSources());
        status.put("classifyEnabled", crawlerProperties.isClassifyEnabled());
        status.put("maxPerSource", crawlerProperties.getMaxPerSource());

        // 检查各数据源状态
        Map<String, Boolean> sourceStatus = new HashMap<>();
        for (SourceClient client : sourceClients) {
            sourceStatus.put(client.getSourceName(), client.isAvailable());
        }
        status.put("sourceStatus", sourceStatus);

        return status;
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            log.info("Shutting down crawler executor service");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
