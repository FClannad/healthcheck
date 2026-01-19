package com.example.crawler.core.model;

import java.util.Map;

/**
 * 爬虫结果
 */
public class CrawlResult {
    
    private String keyword;
    private int found;
    private int saved;
    private long durationMs;
    private Map<String, Integer> sourceStats; // 各数据源的贡献数量
    private String message;
    
    public CrawlResult() {}
    
    public CrawlResult(String keyword, int found, int saved, long durationMs) {
        this.keyword = keyword;
        this.found = found;
        this.saved = saved;
        this.durationMs = durationMs;
    }
    
    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    
    public int getFound() { return found; }
    public void setFound(int found) { this.found = found; }
    
    public int getSaved() { return saved; }
    public void setSaved(int saved) { this.saved = saved; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    public Map<String, Integer> getSourceStats() { return sourceStats; }
    public void setSourceStats(Map<String, Integer> sourceStats) { this.sourceStats = sourceStats; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public double getTps() {
        return durationMs > 0 ? (saved * 1000.0 / durationMs) : 0;
    }
}
