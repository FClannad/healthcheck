package com.example.controller;

import com.example.common.Result;
import com.example.entity.Feedback;
import com.example.service.FeedbackService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Feedback feedback) {
        feedbackService.add(feedback);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Feedback feedback) {
        feedbackService.updateById(feedback);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        feedbackService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        feedbackService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     * 【未使用接口】前端未调用此接口，保留备用
     */
    // @GetMapping("/selectById/{id}")
    // public Result selectById(@PathVariable Integer id) {
    //     Feedback feedback = feedbackService.selectById(id);
    //     return Result.success(feedback);
    // }

    /**
     * 查询所有
     * 【未使用接口】前端未调用此接口，保留备用
     */
    // @GetMapping("/selectAll")
    // public Result selectAll(Feedback feedback) {
    //     List<Feedback> list = feedbackService.selectAll(feedback);
    //     return Result.success(list);
    // }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Feedback feedback,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Feedback> pageInfo = feedbackService.selectPage(feedback, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
