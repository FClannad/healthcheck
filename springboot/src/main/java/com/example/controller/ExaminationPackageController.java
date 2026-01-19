package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExaminationPackage;
import com.example.service.ExaminationPackageService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/examinationPackage")
public class ExaminationPackageController {

    @Resource
    private ExaminationPackageService examinationPackageService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody ExaminationPackage examinationPackage) {
        examinationPackageService.add(examinationPackage);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody ExaminationPackage examinationPackage) {
        examinationPackageService.updateById(examinationPackage);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examinationPackageService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        examinationPackageService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     * 【未使用接口】前端未调用此接口，保留备用
     */
    // @GetMapping("/selectById/{id}")
    // public Result selectById(@PathVariable Integer id) {
    //     ExaminationPackage examinationPackage = examinationPackageService.selectById(id);
    //     return Result.success(examinationPackage);
    // }

    /**
     * 查询所有
     * 【未使用接口】前端未调用此接口，保留备用
     */
    // @GetMapping("/selectAll")
    // public Result selectAll(ExaminationPackage examinationPackage) {
    //     List<ExaminationPackage> list = examinationPackageService.selectAll(examinationPackage);
    //     return Result.success(list);
    // }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(ExaminationPackage examinationPackage,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ExaminationPackage> pageInfo = examinationPackageService.selectPage(examinationPackage, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
