package com.example.mapper;

import com.example.entity.Doctor;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DoctorMapper {

    int insert(Doctor doctor);

    void updateById(Doctor doctor);

    void deleteById(Integer id);

    @Select("select * from `doctor` where id = #{id}")
    Doctor selectById(Integer id);

    @Select("select * from doctor where username = #{username}")
    Doctor selectByUsername(String username);

    List<Doctor> selectAll(Doctor doctor);

    @Select("select count(*) from doctor where title_id = #{titleId}")
    Integer selectByTitleId(Integer titleId);

    @Select("select count(*) from doctor where office_id = #{officeId}")
    Integer selectByOfficeId(Integer officeId);
}
