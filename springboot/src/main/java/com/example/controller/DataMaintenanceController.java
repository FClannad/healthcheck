package com.example.controller;

import com.example.common.Result;
import com.example.mapper.MedicalLiteratureMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据维护控制器 - 用于数据修复和维护
 *
 * 【未使用接口说明】
 * 此控制器的所有接口为后台维护接口，前端未调用。
 * 保留用于数据库维护和健康检查。
 */
@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin
public class DataMaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(DataMaintenanceController.class);

    @Autowired
    private MedicalLiteratureMapper medicalLiteratureMapper;

    /**
     * 修复文献状态 - 将所有NULL或空状态设置为active
     */
    @PostMapping("/fix-literature-status")
    public Result fixLiteratureStatus() {
        try {
            logger.info("开始修复文献状态...");
            
            // 统计修复前的状态
            int totalCount = medicalLiteratureMapper.count();
            
            // 执行状态修复
            int updatedCount = medicalLiteratureMapper.updateNullStatusToActive();
            
            // 统计修复后的状态
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("updatedCount", updatedCount);
            result.put("message", "文献状态修复完成");
            
            logger.info("文献状态修复完成，总数: {}, 修复数量: {}", totalCount, updatedCount);
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("修复文献状态失败", e);
            return Result.error("500", "修复文献状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取文献状态统计
     */
    @GetMapping("/literature-status-stats")
    public Result getLiteratureStatusStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            int totalCount = medicalLiteratureMapper.count();
            int activeCount = medicalLiteratureMapper.countByStatus("active");
            int deletedCount = medicalLiteratureMapper.countByStatus("deleted");
            int nullStatusCount = medicalLiteratureMapper.countByNullStatus();
            
            stats.put("total", totalCount);
            stats.put("active", activeCount);
            stats.put("deleted", deletedCount);
            stats.put("nullStatus", nullStatusCount);
            
            return Result.success(stats);
            
        } catch (Exception e) {
            logger.error("获取文献状态统计失败", e);
            return Result.error("500", "获取文献状态统计失败: " + e.getMessage());
        }
    }

    /**
     * 数据库健康检查
     */
    @GetMapping("/database-health")
    public Result databaseHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // 检查文献表
            int literatureCount = medicalLiteratureMapper.count();
            health.put("literatureCount", literatureCount);
            
            // 检查最近的文献
            if (literatureCount > 0) {
                health.put("hasData", true);
                health.put("status", "UP");
            } else {
                health.put("hasData", false);
                health.put("status", "NO_DATA");
            }
            
            return Result.success(health);
            
        } catch (Exception e) {
            logger.error("数据库健康检查失败", e);
            return Result.error("500", "数据库健康检查失败: " + e.getMessage());
        }
    }
}
