package com.example.crawler.pipeline;

import com.example.entity.MedicalLiterature;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 数据归一化处理器
 */
@Component
public class Normalizer {
    
    /**
     * 归一化文献数据
     */
    public void normalize(List<MedicalLiterature> papers) {
        for (MedicalLiterature paper : papers) {
            normalize(paper);
        }
    }
    
    /**
     * 归一化单个文献
     */
    public void normalize(MedicalLiterature paper) {
        // 标题处理
        if (paper.getTitle() != null) {
            String title = paper.getTitle().trim().replaceAll("\\s+", " ");
            if (title.length() > 500) {
                title = title.substring(0, 497) + "...";
            }
            paper.setTitle(title);
        }
        
        // 作者处理
        if (paper.getAuthors() != null) {
            String authors = paper.getAuthors().trim();
            if (authors.length() > 1000) {
                authors = authors.substring(0, 997) + "...";
            }
            paper.setAuthors(authors);
        }
        
        // 摘要处理
        if (paper.getAbstractContent() != null) {
            String abstractContent = paper.getAbstractContent().trim().replaceAll("\\s+", " ");
            if (abstractContent.length() > 5000) {
                abstractContent = abstractContent.substring(0, 4997) + "...";
            }
            paper.setAbstractContent(abstractContent);
        }
        
        // 设置默认值
        if (paper.getStatus() == null) {
            paper.setStatus("active");
        }
        
        if (paper.getCreateTime() == null) {
            paper.setCreateTime(new Date());
        }
        
        if (paper.getJournal() == null || paper.getJournal().trim().isEmpty()) {
            paper.setJournal("Unknown");
        }
        
        // 关键词处理
        if (paper.getKeywords() != null && paper.getKeywords().length() > 500) {
            paper.setKeywords(paper.getKeywords().substring(0, 497) + "...");
        }
    }
}
