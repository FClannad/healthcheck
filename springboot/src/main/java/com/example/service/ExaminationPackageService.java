package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.entity.ExaminationPackage;
import com.example.entity.PhysicalExamination;
import com.example.mapper.ExaminationPackageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务层方法
 */
@Service
public class ExaminationPackageService {

    @Resource
    private ExaminationPackageMapper examinationPackageMapper;

    @Resource
    PhysicalExaminationService physicalExaminationService;

    public void add(ExaminationPackage examinationPackage) {
        examinationPackageMapper.insert(examinationPackage);
    }

    public void updateById(ExaminationPackage examinationPackage) {
        examinationPackageMapper.updateById(examinationPackage);
    }

    public void deleteById(Integer id) {
        examinationPackageMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            examinationPackageMapper.deleteById(id);
        }
    }

    public ExaminationPackage selectById(Integer id) {
        return examinationPackageMapper.selectById(id);
    }

    public List<ExaminationPackage> selectAll(ExaminationPackage examinationPackage) {
        return examinationPackageMapper.selectAll(examinationPackage);
    }

    public PageInfo<ExaminationPackage> selectPage(ExaminationPackage examinationPackage, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExaminationPackage> list = examinationPackageMapper.selectAll(examinationPackage);
        List<PhysicalExamination> examinationList = new ArrayList<>();
        for (ExaminationPackage p : list) {
            String examinations = p.getExaminations();
            JSONArray examinationIds = JSONUtil.parseArray(examinations);
            for (Object examinationId : examinationIds) {
                PhysicalExamination physicalExamination = physicalExaminationService.selectById((Integer) examinationId);// 根据id查询体检项目
                examinationList.add(physicalExamination);
            }
            p.setExaminationList(examinationList);
        }
        return PageInfo.of(list);
    }

}
