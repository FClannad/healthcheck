package com.example.entity;

import java.util.List;

/**
 * 体检订单
 */
public class ExaminationOrder {
    /** 主键 */
    private Integer id;
    /** 订单号 */
    private String orderNo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getExaminationId() {
        return examinationId;
    }

    public void setExaminationId(Integer examinationId) {
        this.examinationId = examinationId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    /** 用户id */
    private Integer userId;
    /** 医生id */
    private Integer doctorId;
    /** 体检项目id */
    private Integer examinationId;
    /** 订单类型 */
    private String orderType;
    /** 预约日期 */
    private String reserveDate;
    /** 开始时间 */
    private String startTime;
    /** 结束时间 */
    private String endTime;
    /** 检测报告地址 */
    private String file;
    /** 评语 */
    private String comment;
    /** 费用 */
    private Integer money;
    /** 订单状态 */
    private String status;
    /** 创建时间 */
    private String createTime;
    /** 检查时间 */
    private String checkTime;



    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getExaminationName() {
        return examinationName;
    }

    public void setExaminationName(String examinationName) {
        this.examinationName = examinationName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String doctorName;

    private String examinationName;

    private String userName;

    private String feedback;

    private List<PhysicalExamination> examinationList;

    public List<PhysicalExamination> getExaminationList() {
        return examinationList;
    }

    public void setExaminationList(List<PhysicalExamination> examinationList) {
        this.examinationList = examinationList;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
