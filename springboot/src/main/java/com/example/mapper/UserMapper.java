package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    int insert(User user);

    // 批量插入（多值VALUES）
    int batchInsert(@org.apache.ibatis.annotations.Param("users") List<User> users);

    void updateById(User user);

    void deleteById(Integer id);

    @Select("select * from `user` where id = #{id}")
    User selectById(Integer id);

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    List<User> selectAll(User user);

}