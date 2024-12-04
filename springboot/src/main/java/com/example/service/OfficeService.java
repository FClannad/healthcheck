package com.example.service;

import com.example.entity.Office;
import com.example.mapper.OfficeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务层方法
 */
@Service
public class OfficeService {

    @Resource
    private OfficeMapper officeMapper;

    public void add(Office office) {
        officeMapper.insert(office);
    }

    public void updateById(Office office) {
        officeMapper.updateById(office);
    }

    public void deleteById(Integer id) {
        officeMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            officeMapper.deleteById(id);
        }
    }

    public Office selectById(Integer id) {
        return officeMapper.selectById(id);
    }

    public List<Office> selectAll(Office office) {
        return officeMapper.selectAll(office);
    }

    public PageInfo<Office> selectPage(Office office, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Office> list = officeMapper.selectAll(office);
        return PageInfo.of(list);
    }

}