package com.example.mapper;

import com.example.entity.Title;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TitleMapper {

    int insert(Title title);

    void updateById(Title title);

    void deleteById(Integer id);

    @Select("select * from `title` where id = #{id}")
    Title selectById(Integer id);

    List<Title> selectAll(Title title);

}