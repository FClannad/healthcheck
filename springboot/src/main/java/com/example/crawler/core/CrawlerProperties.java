package com.example.crawler.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 爬虫配置属性
 */
@Component
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {
    
    private boolean enabled = true;
    private List<String> sources = Arrays.asList("arxiv", "pubmed");
    private int maxPerSource = 20;
    private boolean classifyEnabled = false;
    private boolean parallel = false;
    private int requestTimeoutMs = 30000;
    private int retryAttempts = 3;
    private int retryDelayMs = 2000;
    
    // 各数据源的具体配置
    private Map<String, SourceConfig> sourceConfigs = new HashMap<>();
    
    public CrawlerProperties() {
        // 默认配置
        sourceConfigs.put("arxiv", new SourceConfig("http://export.arxiv.org/api/query", true, 15));
        sourceConfigs.put("pubmed", new SourceConfig("https://eutils.ncbi.nlm.nih.gov/entrez/eutils", true, 10));
        sourceConfigs.put("ieee", new SourceConfig("", false, 5)); // 默认关闭
    }
    
    public static class SourceConfig {
        private String apiUrl;
        private boolean enabled;
        private int maxResults;
        
        public SourceConfig() {}
        
        public SourceConfig(String apiUrl, boolean enabled, int maxResults) {
            this.apiUrl = apiUrl;
            this.enabled = enabled;
            this.maxResults = maxResults;
        }
        
        // Getters and Setters
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getMaxResults() { return maxResults; }
        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    }
    
    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
    
    public int getMaxPerSource() { return maxPerSource; }
    public void setMaxPerSource(int maxPerSource) { this.maxPerSource = maxPerSource; }
    
    public boolean isClassifyEnabled() { return classifyEnabled; }
    public void setClassifyEnabled(boolean classifyEnabled) { this.classifyEnabled = classifyEnabled; }
    
    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
    
    public int getRequestTimeoutMs() { return requestTimeoutMs; }
    public void setRequestTimeoutMs(int requestTimeoutMs) { this.requestTimeoutMs = requestTimeoutMs; }
    
    public int getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
    
    public int getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(int retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    
    public Map<String, SourceConfig> getSourceConfigs() { return sourceConfigs; }
    public void setSourceConfigs(Map<String, SourceConfig> sourceConfigs) { this.sourceConfigs = sourceConfigs; }
}
