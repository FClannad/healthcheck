package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * AI咨询记录实体类
 */
public class AiConsultation {
    
    private Integer id;
    private Integer userId;           // 用户ID
    private String userQuestion;      // 用户问题
    private String aiResponse;        // AI回复
    private String recommendedExams;  // 推荐的体检项目
    private String sessionId;        // 会话ID
    private String status;           // 状态：active, archived
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;         // 创建时间
    
    // 关联字段
    private String userName;         // 用户姓名
    
    public AiConsultation() {}
    
    public AiConsultation(Integer userId, String userQuestion, String aiResponse) {
        this.userId = userId;
        this.userQuestion = userQuestion;
        this.aiResponse = aiResponse;
        this.createTime = new Date();
        this.status = "active";
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUserQuestion() {
        return userQuestion;
    }
    
    public void setUserQuestion(String userQuestion) {
        this.userQuestion = userQuestion;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public String getRecommendedExams() {
        return recommendedExams;
    }
    
    public void setRecommendedExams(String recommendedExams) {
        this.recommendedExams = recommendedExams;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
