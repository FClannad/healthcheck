package com.example.crawler.core.model;

import java.util.List;

/**
 * 爬虫请求参数
 */
public class CrawlRequest {
    
    private String keyword;
    private int maxResults = 10;
    private List<String> sources; // null表示使用配置的默认源
    private boolean classifyEnabled = false;
    
    public CrawlRequest() {}
    
    public CrawlRequest(String keyword, int maxResults) {
        this.keyword = keyword;
        this.maxResults = maxResults;
    }
    
    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    
    public int getMaxResults() { return maxResults; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
    
    public boolean isClassifyEnabled() { return classifyEnabled; }
    public void setClassifyEnabled(boolean classifyEnabled) { this.classifyEnabled = classifyEnabled; }
}
