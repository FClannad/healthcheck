package com.example.entity;

import java.util.List;

/**
 * 套餐体检项目信息
 */
public class ExaminationPackage {
    /** 主键 */
    private Integer id;
    /** 项目名称 */
    private String name;
    /** 套餐简介 */
    private String descr;
    /** 项目封面 */
    private String cover;
    /** 费用 */
    private Integer money;
    /** 负责医生 */
    private Integer doctorId;
    /** 地址 */
    private String address;
    /** 体检项目列表 */
    private String examinations;

    private String doctorName;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    private List<PhysicalExamination> examinationList;

    public List<PhysicalExamination> getExaminationList() {
        return examinationList;
    }

    public void setExaminationList(List<PhysicalExamination> examinationList) {
        this.examinationList = examinationList;
    }

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

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExaminations() {
        return examinations;
    }

    public void setExaminations(String examinations) {
        this.examinations = examinations;
    }
}