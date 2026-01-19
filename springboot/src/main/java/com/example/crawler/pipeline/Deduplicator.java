package com.example.crawler.pipeline;

import com.example.entity.MedicalLiterature;
import com.example.mapper.MedicalLiteratureMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 去重处理器
 */
@Component
public class Deduplicator {
    
    private static final Logger log = LoggerFactory.getLogger(Deduplicator.class);
    
    @Autowired
    private MedicalLiteratureMapper medicalLiteratureMapper;
    
    /**
     * 去重处理
     * @param papers 原始文献列表
     * @return 去重后的文献列表
     */
    public List<MedicalLiterature> deduplicate(List<MedicalLiterature> papers) {
        List<MedicalLiterature> uniquePapers = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        Set<String> seenUrls = new HashSet<>();
        
        for (MedicalLiterature paper : papers) {
            if (isDuplicate(paper, seenTitles, seenUrls)) {
                log.debug("Duplicate paper found: {}", paper.getTitle());
                continue;
            }
            
            uniquePapers.add(paper);
            
            // 记录已见过的标识
            if (paper.getTitle() != null) {
                seenTitles.add(normalizeTitle(paper.getTitle()));
            }
            if (paper.getSourceUrl() != null) {
                seenUrls.add(paper.getSourceUrl());
            }
        }
        
        log.info("Deduplication: {} -> {} papers", papers.size(), uniquePapers.size());
        return uniquePapers;
    }
    
    /**
     * 检查是否重复
     */
    private boolean isDuplicate(MedicalLiterature paper, Set<String> seenTitles, Set<String> seenUrls) {
        // 1. 检查URL重复
        if (paper.getSourceUrl() != null && seenUrls.contains(paper.getSourceUrl())) {
            return true;
        }
        
        // 2. 检查标题重复（内存中）
        if (paper.getTitle() != null) {
            String normalizedTitle = normalizeTitle(paper.getTitle());
            if (seenTitles.contains(normalizedTitle)) {
                return true;
            }
        }
        
        // 3. 检查数据库中是否存在
        return isDuplicateInDatabase(paper);
    }
    
    /**
     * 检查数据库中是否存在重复
     */
    private boolean isDuplicateInDatabase(MedicalLiterature paper) {
        try {
            // 基于标题和作者检查
            if (paper.getTitle() != null) {
                String normalizedTitle = normalizeTitle(paper.getTitle());
                boolean exists = medicalLiteratureMapper.existsByTitleAndAuthors(
                    normalizedTitle, paper.getAuthors());
                return exists;
            }
        } catch (Exception e) {
            log.warn("Failed to check database duplicate for: {}", paper.getTitle(), e);
        }
        
        return false;
    }
    
    /**
     * 标题标准化
     */
    private String normalizeTitle(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                   .replaceAll("[^a-zA-Z0-9\\s]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
