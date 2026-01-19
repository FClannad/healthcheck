package com.example.entity;

/**
 * 普通体检项目信息
 */
public class PhysicalExamination {
    /** 主键 */
    private Integer id;
    /** 项目名称 */
    private String name;

    public String getExaminationTypeName() {
        return examinationTypeName;
    }

    public void setExaminationTypeName(String examinationTypeName) {
        this.examinationTypeName = examinationTypeName;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    private  String examinationTypeName;

    private  String officeName;

    private  String doctorName;;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Integer getExaminationTypeId() {
        return examinationTypeId;
    }

    public void setExaminationTypeId(Integer examinationTypeId) {
        this.examinationTypeId = examinationTypeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Integer officeId) {
        this.officeId = officeId;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** 项目英文名称 */
    private String englishName;
    /** 项目封面 */
    private String cover;
    /** 项目类型id */
    private Integer examinationTypeId;
    /** 项目内容 */
    private String content;
    /** 适宜人群 */
    private String people;
    /** 检测目的 */
    private String purpose;
    /** 所属科室 */
    private Integer officeId;
    /** 费用 */
    private Integer money;
    /** 负责医生 */
    private Integer doctorId;
    /** 注意事项 */
    private String attention;
    /** 地址 */
    private String address;
}