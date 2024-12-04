package com.example.mapper;

import com.example.entity.ExaminationType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExaminationTypeMapper {

    int insert(ExaminationType examinationType);

    void updateById(ExaminationType examinationType);

    void deleteById(Integer id);

    @Select("select * from `examination_type` where id = #{id}")
    ExaminationType selectById(Integer id);

    List<ExaminationType> selectAll(ExaminationType examinationType);

}