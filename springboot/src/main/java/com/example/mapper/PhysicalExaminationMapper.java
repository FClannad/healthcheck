package com.example.mapper;

import com.example.entity.PhysicalExamination;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PhysicalExaminationMapper {

    int insert(PhysicalExamination physicalExamination);

    void updateById(PhysicalExamination physicalExamination);

    void deleteById(Integer id);


    PhysicalExamination selectById(Integer id);

    List<PhysicalExamination> selectAll(PhysicalExamination physicalExamination);

}