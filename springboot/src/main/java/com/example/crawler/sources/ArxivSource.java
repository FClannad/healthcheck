package com.example.crawler.sources;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.entity.MedicalLiterature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * arXiv数据源
 */
@Component
public class  ArxivSource implements SourceClient {
    
    private static final Logger log = LoggerFactory.getLogger(ArxivSource.class);
    private static final String API_URL = "https://export.arxiv.org/api/query";
    
    @Override
    public String getSourceName() {
        return "arxiv";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            HttpResponse response = HttpRequest.get(API_URL + "?search_query=all:test&max_results=1")
                .timeout(8000)
                .execute();
            boolean available = response.getStatus() == 200 && response.body() != null && response.body().contains("entry");
            log.debug("arXiv availability check: {}", available);
            return available;
        } catch (Exception e) {
            log.warn("arXiv health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<MedicalLiterature> fetch(String keyword, int maxResults) {
        List<MedicalLiterature> papers = new ArrayList<>();
        
        try {
            String query = buildQuery(keyword, maxResults);
            log.debug("Fetching from arXiv: {}", query);
            
            HttpResponse response = HttpRequest.get(query)
                .header("User-Agent", "Academic Research Tool")
                .timeout(30000)
                .execute();
                
            if (response.getStatus() != 200) {
                log.warn("arXiv API request failed with status: {}", response.getStatus());
                return papers;
            }
            
            papers = parseResponse(response.body());
            log.info("Found {} papers from arXiv", papers.size());
            
        } catch (Exception e) {
            log.error("Failed to fetch from arXiv", e);
        }
        
        return papers;
    }
    
    private String buildQuery(String keyword, int maxResults) {
        try {
            StringBuilder query = new StringBuilder(API_URL);
            query.append("?search_query=all:").append(URLEncoder.encode(keyword, "UTF-8"));
            // 移除分类限制，获取更多相关结果
            query.append("&sortBy=submittedDate&sortOrder=descending");
            query.append("&max_results=").append(Math.min(maxResults, 50)); // arXiv限制
            return query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build arXiv query", e);
        }
    }
    
    private List<MedicalLiterature> parseResponse(String xmlContent) {
        List<MedicalLiterature> papers = new ArrayList<>();

        try {
            log.debug("Parsing arXiv XML response, length: {}", xmlContent.length());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // 支持命名空间
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

            // arXiv使用Atom命名空间
            final String ATOM_NS = "http://www.w3.org/2005/Atom";
            NodeList entries = doc.getElementsByTagNameNS(ATOM_NS, "entry");
            if (entries == null || entries.getLength() == 0) {
                // 兼容某些解析器/响应：尝试无命名空间匹配
                entries = doc.getElementsByTagName("entry");
            }
            log.debug("Found {} entries in arXiv response", entries.getLength());

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                MedicalLiterature paper = parseEntry(entry);
                if (paper != null) {
                    papers.add(paper);
                    log.debug("Successfully parsed paper: {}", paper.getTitle());
                } else {
                    log.debug("Failed to parse entry {}", i);
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse arXiv response", e);
            log.debug("XML content preview: {}", xmlContent.substring(0, Math.min(500, xmlContent.length())));
        }

        return papers;
    }
    
    private MedicalLiterature parseEntry(Element entry) {
        try {
            MedicalLiterature paper = new MedicalLiterature();

            // 标题
            String title = getElementText(entry, "title");
            if (title == null || title.trim().isEmpty()) {
                log.debug("Entry missing title, skipping");
                return null;
            }
            paper.setTitle(title.trim().replaceAll("\\s+", " "));

            // 作者
            NodeList authors = entry.getElementsByTagName("author");
            StringBuilder authorStr = new StringBuilder();
            for (int i = 0; i < Math.min(authors.getLength(), 10); i++) {
                Element author = (Element) authors.item(i);
                String name = getElementText(author, "name");
                if (name != null) {
                    if (authorStr.length() > 0) authorStr.append(", ");
                    authorStr.append(name.trim());
                }
            }
            paper.setAuthors(authorStr.toString());

            // 摘要
            String summary = getElementText(entry, "summary");
            if (summary != null) {
                // 清理摘要中的多余空白和换行
                String cleanSummary = summary.trim().replaceAll("\\s+", " ");
                paper.setAbstractContent(cleanSummary);
            }

            // URL
            String id = getElementText(entry, "id");
            if (id != null) {
                paper.setSourceUrl(id.trim());
            }

            // 发布日期
            String published = getElementText(entry, "published");
            if (published != null) {
                paper.setPublishDate(published.substring(0, Math.min(published.length(), 10)));
            }

            // 分类信息
            NodeList categories = entry.getElementsByTagName("category");
            if (categories.getLength() > 0) {
                Element primaryCat = (Element) categories.item(0);
                String term = primaryCat.getAttribute("term");
                if (term != null && !term.isEmpty()) {
                    paper.setKeywords(term);
                }
            }

            // 设置默认值
            paper.setCrawlSource("arXiv");
            paper.setStatus("active");
            paper.setCreateTime(new Date());
            paper.setJournal("arXiv Preprint");

            log.debug("Successfully parsed arXiv paper: {}", paper.getTitle());
            return paper;

        } catch (Exception e) {
            log.warn("Failed to parse arXiv entry", e);
            return null;
        }
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
}
