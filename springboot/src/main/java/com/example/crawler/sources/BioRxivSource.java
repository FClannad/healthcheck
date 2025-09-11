package com.example.crawler.sources;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.entity.MedicalLiterature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * bioRxiv数据源 - 生物医学预印本服务器
 */
@Component
public class BioRxivSource implements SourceClient {
    
    private static final Logger log = LoggerFactory.getLogger(BioRxivSource.class);
    private static final String API_URL = "https://api.biorxiv.org/details/biorxiv";
    
    @Override
    public String getSourceName() {
        return "biorxiv";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            String endDate = java.time.LocalDate.now().toString();
            String startDate = java.time.LocalDate.now().minusDays(1).toString();
            HttpResponse response = HttpRequest.get(API_URL + "/" + startDate + "/" + endDate)
                .timeout(8000)
                .execute();
            return response.getStatus() == 200 && response.body() != null && response.body().contains("collection");
        } catch (Exception e) {
            log.warn("bioRxiv health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<MedicalLiterature> fetch(String keyword, int maxResults) {
        List<MedicalLiterature> papers = new ArrayList<>();
        
        try {
            // bioRxiv API按日期范围获取，我们获取最近的论文然后过滤
            String endDate = java.time.LocalDate.now().toString();
            String startDate = java.time.LocalDate.now().minusDays(30).toString(); // 最近30天
            
            String apiUrl = API_URL + "/" + startDate + "/" + endDate;
            log.debug("Fetching from bioRxiv: {}", apiUrl);
            
            HttpResponse response = HttpRequest.get(apiUrl)
                .header("User-Agent", "Academic Research Tool")
                .timeout(30000)
                .execute();
                
            if (response.getStatus() != 200) {
                log.warn("bioRxiv API request failed with status: {}", response.getStatus());
                return papers;
            }
            
            papers = parseResponse(response.body(), keyword, maxResults);
            log.info("Found {} papers from bioRxiv", papers.size());
            
        } catch (Exception e) {
            log.error("Failed to fetch from bioRxiv", e);
        }
        
        return papers;
    }
    
    private List<MedicalLiterature> parseResponse(String jsonContent, String keyword, int maxResults) {
        List<MedicalLiterature> papers = new ArrayList<>();
        
        try {
            JSONObject response = new JSONObject(jsonContent);
            JSONArray collection = response.getJSONArray("collection");
            
            log.debug("bioRxiv returned {} total papers", collection.size());
            
            for (int i = 0; i < collection.size() && papers.size() < maxResults; i++) {
                JSONObject paper = collection.getJSONObject(i);
                
                // 过滤包含关键词的论文
                String title = paper.getStr("title", "");
                String abstractText = paper.getStr("abstract", "");
                
                if (containsKeyword(title + " " + abstractText, keyword)) {
                    MedicalLiterature literature = parsePaper(paper);
                    if (literature != null) {
                        papers.add(literature);
                        log.debug("Added bioRxiv paper: {}", literature.getTitle());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse bioRxiv response", e);
        }
        
        return papers;
    }
    
    private boolean containsKeyword(String text, String keyword) {
        if (text == null || keyword == null) return false;
        return text.toLowerCase().contains(keyword.toLowerCase());
    }
    
    private MedicalLiterature parsePaper(JSONObject paper) {
        try {
            MedicalLiterature literature = new MedicalLiterature();
            
            // 标题
            String title = paper.getStr("title");
            if (title == null || title.trim().isEmpty()) return null;
            literature.setTitle(title.trim());

            // 作者
            String authors = paper.getStr("authors", "");
            literature.setAuthors(authors);

            // 摘要
            String abstractText = paper.getStr("abstract", "");
            if (abstractText.length() > 5000) {
                abstractText = abstractText.substring(0, 4997) + "...";
            }
            literature.setAbstractContent(abstractText);
            
            // DOI和URL
            String doi = paper.getStr("doi");
            if (doi != null && !doi.isEmpty()) {
                literature.setSourceUrl("https://doi.org/" + doi);
            }

            // 发布日期
            String date = paper.getStr("date");
            if (date != null && date.length() >= 10) {
                literature.setPublishDate(date.substring(0, 10));
            }

            // 分类
            String category = paper.getStr("category", "");
            literature.setKeywords(category);
            
            // 设置默认值
            literature.setCrawlSource("bioRxiv");
            literature.setStatus("active");
            literature.setCreateTime(new Date());
            literature.setJournal("bioRxiv Preprint");
            
            return literature;
            
        } catch (Exception e) {
            log.warn("Failed to parse bioRxiv paper", e);
            return null;
        }
    }
}
