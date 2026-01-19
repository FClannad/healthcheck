package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置类
 */
@Configuration
public class ZhipuAiConfig {
    
    @Value("${api.key:}")
    private String apiKey;
    
    @Value("${api.url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;
    
    @Value("${api.model:glm-4-flash}")
    private String model;
    
    @Value("${api.enabled:true}")
    private boolean enabled;
    
    @Value("${api.timeout:30000}")
    private int timeout;
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
