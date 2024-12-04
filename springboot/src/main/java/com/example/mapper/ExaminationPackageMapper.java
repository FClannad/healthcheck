package com.example.mapper;

import com.example.entity.ExaminationPackage;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExaminationPackageMapper {

    int insert(ExaminationPackage examinationPackage);

    void updateById(ExaminationPackage examinationPackage);

    void deleteById(Integer id);

    @Select("select * from `examination_package` where id = #{id}")
    ExaminationPackage selectById(Integer id);

    List<ExaminationPackage> selectAll(ExaminationPackage examinationPackage);

}
