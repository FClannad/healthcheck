package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private com.example.service.UserBulkService userBulkService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * 批量加载模拟用户数据（性能测试）
     * 示例：POST /user/bulk-load {"count":100000, "mode":"batch", "batchSize":1000}
     * mode: single | batch
     */
    @PostMapping("/bulk-load")
    public Result bulkLoad(@RequestBody Map<String, Object> req) {
        int count = ((Number) req.getOrDefault("count", 10000)).intValue();
        String mode = String.valueOf(req.getOrDefault("mode", "batch"));
        int batchSize = ((Number) req.getOrDefault("batchSize", 1000)).intValue();

        List<User> users = userBulkService.generateUsers(count);
        com.example.service.UserBulkService.BulkResult r;
        if ("single".equalsIgnoreCase(mode)) {
            r = userBulkService.insertSingle(users);
        } else {
            r = userBulkService.insertBatch(users, batchSize);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("requested", r.getRequested());
        data.put("inserted", r.getInserted());
        data.put("millis", r.getMillis());
        data.put("mode", r.getMode());
        data.put("batchSize", r.getBatchSize());
        data.put("tps", r.getMillis() > 0 ? (r.getInserted() * 1000.0 / r.getMillis()) : null);
        return Result.success(data);
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> pageInfo = userService.selectPage(user, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
