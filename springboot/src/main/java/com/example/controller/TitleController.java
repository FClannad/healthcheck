package com.example.controller;

import com.example.common.Result;
import com.example.entity.Title;
import com.example.service.TitleService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/title")
public class TitleController {

    @Resource
    private TitleService titleService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Title title) {
        titleService.add(title);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Title title) {
        titleService.updateById(title);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        titleService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        titleService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Title title = titleService.selectById(id);
        return Result.success(title);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Title title) {
        List<Title> list = titleService.selectAll(title);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Title title,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Title> pageInfo = titleService.selectPage(title, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}