package com.example.controller;

import com.example.common.Result;
import com.example.entity.PhysicalExamination;
import com.example.service.PhysicalExaminationService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/physicalExamination")
public class PhysicalExaminationController {

    @Resource
    private PhysicalExaminationService physicalExaminationService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody PhysicalExamination physicalExamination) {
        physicalExaminationService.add(physicalExamination);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody PhysicalExamination physicalExamination) {
        physicalExaminationService.updateById(physicalExamination);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        physicalExaminationService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        physicalExaminationService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        PhysicalExamination physicalExamination = physicalExaminationService.selectById(id);
        return Result.success(physicalExamination);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(PhysicalExamination physicalExamination) {
        List<PhysicalExamination> list = physicalExaminationService.selectAll(physicalExamination);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(PhysicalExamination physicalExamination,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<PhysicalExamination> pageInfo = physicalExaminationService.selectPage(physicalExamination, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}