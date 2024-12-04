package com.example.controller;

import com.example.common.Result;
import com.example.entity.Office;
import com.example.service.OfficeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/office")
public class OfficeController {

    @Resource
    private OfficeService officeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Office office) {
        officeService.add(office);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Office office) {
        officeService.updateById(office);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        officeService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        officeService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Office office = officeService.selectById(id);
        return Result.success(office);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Office office) {
        List<Office> list = officeService.selectAll(office);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Office office,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Office> pageInfo = officeService.selectPage(office, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}