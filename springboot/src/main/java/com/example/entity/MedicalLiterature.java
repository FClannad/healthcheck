package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 医疗文献实体类 - 简化版
 */
public class MedicalLiterature {

    private Integer id;
    private String title;                // 文献标题
    private String authors;              // 作者
    private String journal;              // 期刊名称
    private String publishDate;          // 发布日期
    private String abstractContent;      // 摘要内容
    private String keywords;             // 关键词
    private String sourceUrl;            // 原文链接
    private String crawlSource;          // 爬取来源（PubMed、arXiv等）
    private String status;               // 状态（active、deleted）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;             // 创建时间

    // 构造函数
    public MedicalLiterature() {}

    public MedicalLiterature(String title, String authors, String journal) {
        this.title = title;
        this.authors = authors;
        this.journal = journal;
        this.createTime = new Date();
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getAbstractContent() {
        return abstractContent;
    }

    public void setAbstractContent(String abstractContent) {
        this.abstractContent = abstractContent;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }



    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCrawlSource() {
        return crawlSource;
    }

    public void setCrawlSource(String crawlSource) {
        this.crawlSource = crawlSource;
    }

    @Override
    public String toString() {
        return "MedicalLiterature{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", journal='" + journal + '\'' +
                ", crawlSource='" + crawlSource + '\'' +
                '}';
    }
}