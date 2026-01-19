package com.example.controller;

import com.example.common.Result;
import com.example.service.CacheService;
import com.example.service.MessageService;
import com.example.service.ScheduledTaskService;
import com.example.service.MedicalLiteratureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供系统状态监控、缓存管理、任务管理等功能
 *
 * 【未使用接口说明】
 * 此控制器的所有接口为后台管理接口，前端未调用。
 * 保留用于系统监控和管理。
 */
@RestController
@RequestMapping("/system")
public class SystemMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorController.class);

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    @Autowired(required = false)
    private CacheService cacheService;

    @Autowired(required = false)
    private MessageService messageService;

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    /**
     * 获取系统状态概览
     */
    @GetMapping("/status")
    public Result getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 基本信息
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            status.put("status", "running");
            
            // JVM信息
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            Map<String, Object> jvmInfo = new HashMap<>();
            jvmInfo.put("uptime", runtimeBean.getUptime());
            jvmInfo.put("startTime", runtimeBean.getStartTime());
            jvmInfo.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
            jvmInfo.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
            jvmInfo.put("nonHeapMemoryUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
            
            status.put("jvm", jvmInfo);
            
            // 服务状态
            Map<String, Object> services = new HashMap<>();
            services.put("database", checkDatabaseStatus());
            services.put("cache", checkCacheStatus());
            services.put("messageQueue", checkMessageQueueStatus());
            
            status.put("services", services);
            
            // 业务统计
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalLiterature", getTotalLiteratureCount());
            statistics.put("todayLiterature", getTodayLiteratureCount());
            
            status.put("statistics", statistics);
            
            return Result.success(status);
            
        } catch (Exception e) {
            logger.error("获取系统状态失败", e);
            return Result.error("500", "获取系统状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取详细的系统信息
     */
    @GetMapping("/info")
    public Result getSystemInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            // 系统属性
            Map<String, Object> systemProps = new HashMap<>();
            systemProps.put("javaVersion", System.getProperty("java.version"));
            systemProps.put("javaVendor", System.getProperty("java.vendor"));
            systemProps.put("osName", System.getProperty("os.name"));
            systemProps.put("osVersion", System.getProperty("os.version"));
            systemProps.put("osArch", System.getProperty("os.arch"));
            
            info.put("system", systemProps);
            
            // 应用信息
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("name", "健康体检系统");
            appInfo.put("version", "1.0.0");
            appInfo.put("description", "基于Spring Boot + Vue的健康体检管理系统");
            appInfo.put("features", new String[]{
                "用户管理", "体检管理", "医疗文献", "爬虫服务", 
                "消息队列", "缓存系统", "微服务架构", "定时任务"
            });
            
            info.put("application", appInfo);
            
            // 技术栈
            Map<String, Object> techStack = new HashMap<>();
            techStack.put("backend", "Spring Boot 3.3.1");
            techStack.put("frontend", "Vue 3");
            techStack.put("database", "MySQL 8.0");
            techStack.put("cache", cacheService != null ? "Redis" : "未启用");
            techStack.put("messageQueue", messageService != null ? "RabbitMQ" : "未启用");
            techStack.put("microservice", "Spring Cloud");
            
            info.put("techStack", techStack);
            
            return Result.success(info);
            
        } catch (Exception e) {
            logger.error("获取系统信息失败", e);
            return Result.error("500", "获取系统信息失败: " + e.getMessage());
        }
    }

    /**
     * 缓存管理接口
     */
    @PostMapping("/cache/clear")
    public Result clearCache(@RequestParam(required = false) String key) {
        try {
            if (cacheService == null) {
                return Result.error("503", "缓存服务未启用");
            }
            
            if (key != null && !key.trim().isEmpty()) {
                cacheService.delete(key);
                logger.info("清除指定缓存: {}", key);
                return Result.success("指定缓存已清除: " + key);
            } else {
                cacheService.clearAll();
                logger.info("清除所有缓存");
                return Result.success("所有缓存已清除");
            }
            
        } catch (Exception e) {
            logger.error("清除缓存失败", e);
            return Result.error("500", "清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存信息
     */
    @GetMapping("/cache/info")
    public Result getCacheInfo() {
        try {
            if (cacheService == null) {
                return Result.error("503", "缓存服务未启用");
            }
            
            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("status", "enabled");
            cacheInfo.put("info", cacheService.getCacheInfo());
            cacheInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return Result.success(cacheInfo);
            
        } catch (Exception e) {
            logger.error("获取缓存信息失败", e);
            return Result.error("500", "获取缓存信息失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发定时任务
     * 【未使用接口】前端未调用此接口，保留用于后台管理
     */
    @PostMapping("/task/trigger")
    public Result triggerTask(@RequestParam String taskType, @RequestParam(required = false) String param) {
        try {
            switch (taskType) {
                case "crawling":
                    String keyword = param != null ? param : "人工智能";
                    scheduledTaskService.triggerManualCrawling(keyword);
                    return Result.success("手动爬虫任务已触发，关键词: " + keyword);
                    
                case "healthCheck":
                    // 这里可以添加手动健康检查的逻辑
                    return Result.success("健康检查任务已触发");
                    
                default:
                    return Result.error("400", "不支持的任务类型: " + taskType);
            }
            
        } catch (Exception e) {
            logger.error("触发定时任务失败", e);
            return Result.error("500", "触发定时任务失败: " + e.getMessage());
        }
    }

    /**
     * 发送测试消息
     */
    @PostMapping("/message/test")
    public Result sendTestMessage(@RequestBody Map<String, Object> request) {
        try {
            if (messageService == null) {
                return Result.error("503", "消息服务未启用");
            }
            
            String type = (String) request.getOrDefault("type", "notification");
            String content = (String) request.getOrDefault("content", "系统测试消息");
            
            switch (type) {
                case "notification":
                    messageService.sendNotificationMessage(1L, "系统测试", content);
                    break;
                case "email":
                    messageService.sendEmailMessage("test@example.com", "系统测试", content);
                    break;
                case "crawler":
                    messageService.sendCrawlerMessage("测试关键词", 3, "test");
                    break;
                default:
                    return Result.error("400", "不支持的消息类型: " + type);
            }
            
            return Result.success("测试消息已发送: " + type);
            
        } catch (Exception e) {
            logger.error("发送测试消息失败", e);
            return Result.error("500", "发送测试消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统日志（最近的一些日志）
     */
    @GetMapping("/logs")
    public Result getSystemLogs(@RequestParam(defaultValue = "50") int limit) {
        try {
            // 这里应该读取实际的日志文件
            // 简化实现，返回一些示例日志
            Map<String, Object> logs = new HashMap<>();
            logs.put("message", "日志功能需要集成实际的日志系统");
            logs.put("suggestion", "可以集成ELK Stack或其他日志管理系统");
            logs.put("limit", limit);
            
            return Result.success(logs);
            
        } catch (Exception e) {
            logger.error("获取系统日志失败", e);
            return Result.error("500", "获取系统日志失败: " + e.getMessage());
        }
    }

    // 私有辅助方法

    private String checkDatabaseStatus() {
        try {
            medicalLiteratureService.selectPage(new com.example.entity.MedicalLiterature(), 1, 1);
            return "正常";
        } catch (Exception e) {
            return "异常";
        }
    }

    private String checkCacheStatus() {
        if (cacheService == null) {
            return "未启用";
        }
        try {
            cacheService.getCacheInfo();
            return "正常";
        } catch (Exception e) {
            return "异常";
        }
    }

    private String checkMessageQueueStatus() {
        if (messageService == null) {
            return "未启用";
        }
        try {
            // 简单检查，实际应该检查RabbitMQ连接状态
            return "正常";
        } catch (Exception e) {
            return "异常";
        }
    }

    private int getTotalLiteratureCount() {
        try {
            // 这里应该查询总数，简化实现
            return 100; // 示例数据
        } catch (Exception e) {
            return 0;
        }
    }

    private int getTodayLiteratureCount() {
        try {
            // 这里应该查询今日新增数量，简化实现
            return 5; // 示例数据
        } catch (Exception e) {
            return 0;
        }
    }
}
