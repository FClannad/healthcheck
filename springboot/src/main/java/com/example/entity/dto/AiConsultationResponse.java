package com.example.entity.dto;

import com.example.entity.ExaminationPackage;

import java.util.List;

/**
 * AI咨询响应DTO
 */
public class AiConsultationResponse {
    
    private String response;                    // AI回复内容
    private List<String> recommendedExams;     // 推荐的体检项目
    private List<ExaminationPackage> recommendedPackages; // 推荐的体检套餐
    private String sessionId;                  // 会话ID
    private Boolean needMoreInfo;              // 是否需要更多信息
    private String followUpQuestion;           // 后续问题建议
    
    public AiConsultationResponse() {}
    
    public AiConsultationResponse(String response, List<String> recommendedExams) {
        this.response = response;
        this.recommendedExams = recommendedExams;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public List<String> getRecommendedExams() {
        return recommendedExams;
    }
    
    public void setRecommendedExams(List<String> recommendedExams) {
        this.recommendedExams = recommendedExams;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Boolean getNeedMoreInfo() {
        return needMoreInfo;
    }
    
    public void setNeedMoreInfo(Boolean needMoreInfo) {
        this.needMoreInfo = needMoreInfo;
    }
    
    public String getFollowUpQuestion() {
        return followUpQuestion;
    }
    
    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }

    public List<ExaminationPackage> getRecommendedPackages() {
        return recommendedPackages;
    }

    public void setRecommendedPackages(List<ExaminationPackage> recommendedPackages) {
        this.recommendedPackages = recommendedPackages;
    }
}
