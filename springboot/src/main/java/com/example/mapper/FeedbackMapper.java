package com.example.mapper;

import com.example.entity.Feedback;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FeedbackMapper {

    int insert(Feedback feedback);

    void updateById(Feedback feedback);

    void deleteById(Integer id);

    @Select("select * from `feedback` where id = #{id}")
    Feedback selectById(Integer id);

    List<Feedback> selectAll(Feedback feedback);

}
