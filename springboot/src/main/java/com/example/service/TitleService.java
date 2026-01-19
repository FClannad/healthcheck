package com.example.service;

import com.example.entity.Title;
import com.example.mapper.TitleMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务层方法
 */
@Service
public class TitleService {

    @Resource
    private TitleMapper titleMapper;

    public void add(Title title) {
        titleMapper.insert(title);
    }

    public void updateById(Title title) {
        titleMapper.updateById(title);
    }

    public void deleteById(Integer id) {
        titleMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            titleMapper.deleteById(id);
        }
    }

    public Title selectById(Integer id) {
        return titleMapper.selectById(id);
    }

    public List<Title> selectAll(Title title) {
        return titleMapper.selectAll(title);
    }

    public PageInfo<Title> selectPage(Title title, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Title> list = titleMapper.selectAll(title);
        return PageInfo.of(list);
    }

}