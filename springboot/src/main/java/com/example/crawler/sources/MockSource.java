package com.example.crawler.sources;

import com.example.entity.MedicalLiterature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 模拟数据源 - 用于测试爬虫功能
 * 生成与关键词相关的模拟医学文献数据
 */
@Component
public class MockSource implements SourceClient {

    private final Random random = new Random();
    
    // 预定义的医学期刊
    private final String[] journals = {
        "Nature Medicine", "The Lancet", "New England Journal of Medicine",
        "JAMA", "BMJ", "Cell", "Science", "Nature", "PLoS Medicine",
        "Journal of Clinical Investigation", "Circulation", "Cancer Research"
    };
    
    // 预定义的作者名
    private final String[] authors = {
        "Dr. Smith", "Dr. Johnson", "Dr. Williams", "Dr. Brown", "Dr. Jones",
        "Dr. Garcia", "Dr. Miller", "Dr. Davis", "Dr. Rodriguez", "Dr. Martinez",
        "Dr. Anderson", "Dr. Taylor", "Dr. Thomas", "Dr. Hernandez", "Dr. Moore"
    };

    @Override
    public String getSourceName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        // 模拟数据源始终可用
        return true;
    }

    @Override
    public List<MedicalLiterature> fetch(String keyword, int maxResults) {
        List<MedicalLiterature> papers = new ArrayList<>();
        
        // 生成1-maxResults篇文献
        int count = Math.min(maxResults, random.nextInt(maxResults) + 1);
        
        for (int i = 0; i < count; i++) {
            MedicalLiterature paper = generatePaper(keyword, i + 1);
            papers.add(paper);
        }
        
        // 模拟网络延迟
        try {
            Thread.sleep(100 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return papers;
    }

    private MedicalLiterature generatePaper(String keyword, int index) {
        MedicalLiterature paper = new MedicalLiterature();
        
        // 生成标题
        paper.setTitle(generateTitle(keyword, index));
        
        // 生成作者
        paper.setAuthors(generateAuthors());
        
        // 选择期刊
        paper.setJournal(journals[random.nextInt(journals.length)]);
        
        // 生成发布日期（最近2年内）
        LocalDateTime publishDate = LocalDateTime.now().minusDays(random.nextInt(730));
        paper.setPublishDate(publishDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // 生成摘要
        paper.setAbstractContent(generateAbstract(keyword));
        
        // 生成关键词
        paper.setKeywords(generateKeywords(keyword));
        
        // 设置分类 - 移除category字段，分类信息包含在keywords中
        // paper.setCategory("医学研究");
        
        // 生成URL
        paper.setSourceUrl("https://mock-source.example.com/paper/" + 
            keyword.toLowerCase() + "/" + index);
        
        // 设置爬取源
        paper.setCrawlSource("mock");
        
        // 设置创建时间
        paper.setCreateTime(new Date());
        
        return paper;
    }

    private String generateTitle(String keyword, int index) {
        String[] templates = {
            "Clinical Study of %s in %s Treatment: A Randomized Controlled Trial",
            "Novel Approaches to %s Research: Implications for %s Therapy",
            "The Role of %s in Modern %s Medicine: A Comprehensive Review",
            "Advances in %s Diagnosis and %s Management: Current Perspectives",
            "Molecular Mechanisms of %s: New Insights into %s Pathophysiology",
            "%s Biomarkers in %s: Diagnostic and Therapeutic Applications",
            "Innovative %s Treatments: A Systematic Review of %s Interventions",
            "Epidemiological Trends in %s: Impact on %s Healthcare"
        };
        
        String template = templates[random.nextInt(templates.length)];
        String relatedTerm = generateRelatedTerm(keyword);
        
        return String.format(template, keyword, relatedTerm) + " (Study #" + index + ")";
    }

    private String generateAuthors() {
        int authorCount = 2 + random.nextInt(4); // 2-5个作者
        List<String> selectedAuthors = new ArrayList<>();
        
        for (int i = 0; i < authorCount; i++) {
            String author = authors[random.nextInt(authors.length)];
            if (!selectedAuthors.contains(author)) {
                selectedAuthors.add(author);
            }
        }
        
        return String.join(", ", selectedAuthors);
    }

    private String generateAbstract(String keyword) {
        String[] templates = {
            "Background: %s has emerged as a significant factor in modern healthcare. " +
            "This study investigates the clinical implications and therapeutic potential of %s-related interventions. " +
            "Methods: We conducted a comprehensive analysis involving 200 patients with %s-related conditions. " +
            "Results: Our findings demonstrate significant improvements in patient outcomes when %s protocols are implemented. " +
            "Conclusion: These results suggest that %s-based approaches may revolutionize current treatment paradigms.",
            
            "Objective: To evaluate the effectiveness of %s in clinical practice and assess its impact on patient care. " +
            "Design: Prospective cohort study examining %s interventions over a 12-month period. " +
            "Participants: Adult patients presenting with %s-related symptoms were enrolled in this study. " +
            "Main outcomes: Primary endpoints included %s response rates and safety profiles. " +
            "Results: Significant improvements were observed in %s-treated groups compared to controls.",
            
            "Introduction: Recent advances in %s research have opened new avenues for therapeutic intervention. " +
            "This review synthesizes current knowledge about %s mechanisms and clinical applications. " +
            "Methods: Systematic literature review of %s studies published between 2020-2024. " +
            "Findings: Evidence supports the efficacy of %s in multiple clinical scenarios. " +
            "Implications: These findings have important implications for %s treatment guidelines."
        };
        
        String template = templates[random.nextInt(templates.length)];
        return String.format(template, keyword, keyword, keyword, keyword, keyword);
    }

    private String generateKeywords(String keyword) {
        String[] commonKeywords = {
            "clinical trial", "treatment", "diagnosis", "therapy", "research",
            "medicine", "healthcare", "patient care", "biomarker", "intervention"
        };
        
        List<String> keywords = new ArrayList<>();
        keywords.add(keyword);
        
        // 添加2-4个相关关键词
        int count = 2 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            String kw = commonKeywords[random.nextInt(commonKeywords.length)];
            if (!keywords.contains(kw)) {
                keywords.add(kw);
            }
        }
        
        return String.join(", ", keywords);
    }

    private String generateRelatedTerm(String keyword) {
        // 根据关键词生成相关术语
        switch (keyword.toLowerCase()) {
            case "cancer":
                return "oncology";
            case "diabetes":
                return "endocrinology";
            case "heart":
                return "cardiology";
            case "brain":
                return "neurology";
            case "eye":
                return "ophthalmology";
            case "lung":
                return "pulmonology";
            default:
                return "medicine";
        }
    }
}
