package com.example.mapper;

import com.example.entity.Office;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OfficeMapper {

    int insert(Office office);

    void updateById(Office office);

    void deleteById(Integer id);

    @Select("select * from `office` where id = #{id}")
    Office selectById(Integer id);

    List<Office> selectAll(Office office);

}