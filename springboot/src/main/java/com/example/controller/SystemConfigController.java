package com.example.controller;

import com.example.common.Result;
import com.example.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供系统配置的增删改查功能
 */
@RestController
@RequestMapping("/system/config")
public class SystemConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigController.class);

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping("/all")
    public Result getAllConfigs() {
        try {
            logger.info("获取所有系统配置");
            
            Map<String, Object> result = new HashMap<>();
            
            // 获取所有配置
            Map<String, Object> configs = systemConfigService.getAllConfigs();
            result.put("configs", configs);
            
            // 获取配置描述
            Map<String, String> descriptions = systemConfigService.getConfigDescriptions();
            result.put("descriptions", descriptions);
            
            // 配置分组
            Map<String, Object> groups = groupConfigs(configs);
            result.put("groups", groups);
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("获取系统配置失败", e);
            return Result.error("500", "获取系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定配置
     */
    @GetMapping("/{key}")
    public Result getConfig(@PathVariable String key) {
        try {
            logger.info("获取配置: {}", key);
            
            Object value = systemConfigService.getConfig(key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("description", systemConfigService.getConfigDescriptions().get(key));
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("获取配置失败: {}", key, e);
            return Result.error("500", "获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置配置
     */
    @PostMapping("/{key}")
    public Result setConfig(@PathVariable String key, @RequestBody Map<String, Object> request) {
        try {
            Object value = request.get("value");
            
            logger.info("设置配置: {} = {}", key, value);
            
            // 验证配置值
            if (!systemConfigService.validateConfig(key, value)) {
                return Result.error("400", "配置值不合法: " + value);
            }
            
            systemConfigService.setConfig(key, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("message", "配置设置成功");
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("设置配置失败: {}", key, e);
            return Result.error("500", "设置配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量设置配置
     */
    @PostMapping("/batch")
    public Result setConfigs(@RequestBody Map<String, Object> configs) {
        try {
            logger.info("批量设置配置，数量: {}", configs.size());
            
            // 验证所有配置
            for (Map.Entry<String, Object> entry : configs.entrySet()) {
                if (!systemConfigService.validateConfig(entry.getKey(), entry.getValue())) {
                    return Result.error("400", "配置值不合法: " + entry.getKey() + " = " + entry.getValue());
                }
            }
            
            systemConfigService.setConfigs(configs);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", configs.size());
            result.put("message", "批量配置设置成功");
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("批量设置配置失败", e);
            return Result.error("500", "批量设置配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{key}")
    public Result removeConfig(@PathVariable String key) {
        try {
            logger.info("删除配置: {}", key);
            
            systemConfigService.removeConfig(key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("message", "配置删除成功");
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("删除配置失败: {}", key, e);
            return Result.error("500", "删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 重置所有配置为默认值
     */
    @PostMapping("/reset")
    public Result resetConfigs() {
        try {
            logger.info("重置所有配置为默认值");
            
            systemConfigService.resetToDefaults();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "配置重置成功");
            result.put("configs", systemConfigService.getAllConfigs());
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("重置配置失败", e);
            return Result.error("500", "重置配置失败: " + e.getMessage());
        }
    }

    /**
     * 验证配置值
     */
    @PostMapping("/validate")
    public Result validateConfig(@RequestBody Map<String, Object> request) {
        try {
            String key = (String) request.get("key");
            Object value = request.get("value");
            
            logger.info("验证配置: {} = {}", key, value);
            
            boolean isValid = systemConfigService.validateConfig(key, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("valid", isValid);
            
            if (!isValid) {
                result.put("message", "配置值不合法");
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("验证配置失败", e);
            return Result.error("500", "验证配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置模板
     */
    @GetMapping("/template")
    public Result getConfigTemplate() {
        try {
            logger.info("获取配置模板");
            
            Map<String, Object> template = new HashMap<>();
            
            // 爬虫配置模板
            Map<String, Object> crawlerTemplate = new HashMap<>();
            crawlerTemplate.put("enabled", true);
            crawlerTemplate.put("maxConcurrent", 3);
            crawlerTemplate.put("defaultCount", 10);
            crawlerTemplate.put("timeoutSeconds", 30);
            template.put("crawler", crawlerTemplate);
            
            // 缓存配置模板
            Map<String, Object> cacheTemplate = new HashMap<>();
            cacheTemplate.put("enabled", true);
            cacheTemplate.put("defaultTtlMinutes", 30);
            template.put("cache", cacheTemplate);
            
            // 消息队列配置模板
            Map<String, Object> mqTemplate = new HashMap<>();
            mqTemplate.put("enabled", true);
            mqTemplate.put("retryCount", 3);
            template.put("messageQueue", mqTemplate);
            
            // 系统配置模板
            Map<String, Object> systemTemplate = new HashMap<>();
            systemTemplate.put("maintenanceMode", false);
            systemTemplate.put("maxUploadSizeMb", 10);
            systemTemplate.put("sessionTimeoutMinutes", 120);
            template.put("system", systemTemplate);
            
            return Result.success(template);
            
        } catch (Exception e) {
            logger.error("获取配置模板失败", e);
            return Result.error("500", "获取配置模板失败: " + e.getMessage());
        }
    }

    /**
     * 导出配置
     */
    @GetMapping("/export")
    public Result exportConfigs(@RequestParam(defaultValue = "json") String format) {
        try {
            logger.info("导出配置，格式: {}", format);
            
            Map<String, Object> configs = systemConfigService.getAllConfigs();
            
            Map<String, Object> result = new HashMap<>();
            result.put("format", format);
            result.put("exportTime", System.currentTimeMillis());
            result.put("configs", configs);
            result.put("descriptions", systemConfigService.getConfigDescriptions());
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("导出配置失败", e);
            return Result.error("500", "导出配置失败: " + e.getMessage());
        }
    }

    /**
     * 导入配置
     */
    @PostMapping("/import")
    public Result importConfigs(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configs = (Map<String, Object>) request.get("configs");
            
            if (configs == null || configs.isEmpty()) {
                return Result.error("400", "导入的配置数据为空");
            }
            
            logger.info("导入配置，数量: {}", configs.size());
            
            // 验证所有配置
            for (Map.Entry<String, Object> entry : configs.entrySet()) {
                if (!systemConfigService.validateConfig(entry.getKey(), entry.getValue())) {
                    return Result.error("400", "配置值不合法: " + entry.getKey() + " = " + entry.getValue());
                }
            }
            
            systemConfigService.setConfigs(configs);
            
            Map<String, Object> result = new HashMap<>();
            result.put("importedCount", configs.size());
            result.put("message", "配置导入成功");
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("导入配置失败", e);
            return Result.error("500", "导入配置失败: " + e.getMessage());
        }
    }

    // 私有辅助方法

    private Map<String, Object> groupConfigs(Map<String, Object> configs) {
        Map<String, Object> groups = new HashMap<>();
        
        Map<String, Object> crawlerGroup = new HashMap<>();
        Map<String, Object> cacheGroup = new HashMap<>();
        Map<String, Object> mqGroup = new HashMap<>();
        Map<String, Object> systemGroup = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key.startsWith("crawler.")) {
                crawlerGroup.put(key, value);
            } else if (key.startsWith("cache.")) {
                cacheGroup.put(key, value);
            } else if (key.startsWith("mq.")) {
                mqGroup.put(key, value);
            } else if (key.startsWith("system.")) {
                systemGroup.put(key, value);
            }
        }
        
        groups.put("crawler", crawlerGroup);
        groups.put("cache", cacheGroup);
        groups.put("messageQueue", mqGroup);
        groups.put("system", systemGroup);
        
        return groups;
    }
}
