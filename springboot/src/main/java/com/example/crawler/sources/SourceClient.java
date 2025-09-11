package com.example.crawler.sources;

import com.example.entity.MedicalLiterature;

import java.util.List;

/**
 * 数据源客户端接口
 */
public interface SourceClient {
    
    /**
     * 获取数据源名称
     */
    String getSourceName();
    
    /**
     * 检查数据源是否可用
     */
    boolean isAvailable();
    
    /**
     * 从数据源获取文献
     * @param keyword 搜索关键词
     * @param maxResults 最大结果数
     * @return 文献列表
     */
    List<MedicalLiterature> fetch(String keyword, int maxResults);
}
