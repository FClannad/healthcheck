package com.example.entity.dto;

/**
 * AI咨询请求DTO
 */
public class AiConsultationRequest {
    
    private String question;      // 用户问题
    private String sessionId;    // 会话ID（可选，用于上下文对话）
    private Integer userId;      // 用户ID
    
    public AiConsultationRequest() {}
    
    public AiConsultationRequest(String question, Integer userId) {
        this.question = question;
        this.userId = userId;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
