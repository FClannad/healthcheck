package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置管理服务
 * 提供动态配置管理功能
 */
@Service
public class SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    @Autowired(required = false)
    private CacheService cacheService;

    // 内存中的配置缓存
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();

    // 配置键常量
    public static final String CRAWLER_ENABLED = "crawler.enabled";
    public static final String CRAWLER_MAX_CONCURRENT = "crawler.max.concurrent";
    public static final String CRAWLER_DEFAULT_COUNT = "crawler.default.count";
    public static final String CRAWLER_TIMEOUT = "crawler.timeout.seconds";
    
    public static final String CACHE_ENABLED = "cache.enabled";
    public static final String CACHE_DEFAULT_TTL = "cache.default.ttl.minutes";
    
    public static final String MQ_ENABLED = "mq.enabled";
    public static final String MQ_RETRY_COUNT = "mq.retry.count";
    
    public static final String SYSTEM_MAINTENANCE_MODE = "system.maintenance.mode";
    public static final String SYSTEM_MAX_UPLOAD_SIZE = "system.max.upload.size.mb";
    public static final String SYSTEM_SESSION_TIMEOUT = "system.session.timeout.minutes";

    /**
     * 初始化默认配置
     */
    public void initDefaultConfigs() {
        logger.info("初始化系统默认配置");
        
        // 爬虫相关配置
        setConfigIfNotExists(CRAWLER_ENABLED, true);
        setConfigIfNotExists(CRAWLER_MAX_CONCURRENT, 3);
        setConfigIfNotExists(CRAWLER_DEFAULT_COUNT, 10);
        setConfigIfNotExists(CRAWLER_TIMEOUT, 30);
        
        // 缓存相关配置
        setConfigIfNotExists(CACHE_ENABLED, true);
        setConfigIfNotExists(CACHE_DEFAULT_TTL, 30);
        
        // 消息队列相关配置
        setConfigIfNotExists(MQ_ENABLED, true);
        setConfigIfNotExists(MQ_RETRY_COUNT, 3);
        
        // 系统相关配置
        setConfigIfNotExists(SYSTEM_MAINTENANCE_MODE, false);
        setConfigIfNotExists(SYSTEM_MAX_UPLOAD_SIZE, 10);
        setConfigIfNotExists(SYSTEM_SESSION_TIMEOUT, 120);
        
        logger.info("系统默认配置初始化完成");
    }

    /**
     * 获取配置值
     */
    public Object getConfig(String key) {
        try {
            // 先从内存缓存获取
            Object value = configCache.get(key);
            if (value != null) {
                return value;
            }
            
            // 从Redis缓存获取
            if (cacheService != null) {
                value = cacheService.get("config:" + key);
                if (value != null) {
                    configCache.put(key, value);
                    return value;
                }
            }
            
            // 返回默认值
            return getDefaultValue(key);
            
        } catch (Exception e) {
            logger.error("获取配置失败: {}", key, e);
            return getDefaultValue(key);
        }
    }

    /**
     * 获取配置值并转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, Class<T> clazz) {
        Object value = getConfig(key);
        if (value == null) {
            return null;
        }
        
        try {
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            
            // 类型转换
            if (clazz == String.class) {
                return (T) String.valueOf(value);
            } else if (clazz == Integer.class) {
                return (T) Integer.valueOf(String.valueOf(value));
            } else if (clazz == Long.class) {
                return (T) Long.valueOf(String.valueOf(value));
            } else if (clazz == Boolean.class) {
                return (T) Boolean.valueOf(String.valueOf(value));
            } else if (clazz == Double.class) {
                return (T) Double.valueOf(String.valueOf(value));
            }
            
            return (T) value;
            
        } catch (Exception e) {
            logger.error("配置类型转换失败: {} -> {}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 设置配置值
     */
    public void setConfig(String key, Object value) {
        try {
            // 更新内存缓存
            configCache.put(key, value);
            
            // 更新Redis缓存
            if (cacheService != null) {
                cacheService.set("config:" + key, value);
            }
            
            logger.info("配置已更新: {} = {}", key, value);
            
        } catch (Exception e) {
            logger.error("设置配置失败: {} = {}", key, value, e);
        }
    }

    /**
     * 删除配置
     */
    public void removeConfig(String key) {
        try {
            // 从内存缓存删除
            configCache.remove(key);
            
            // 从Redis缓存删除
            if (cacheService != null) {
                cacheService.delete("config:" + key);
            }
            
            logger.info("配置已删除: {}", key);
            
        } catch (Exception e) {
            logger.error("删除配置失败: {}", key, e);
        }
    }

    /**
     * 获取所有配置
     */
    public Map<String, Object> getAllConfigs() {
        Map<String, Object> allConfigs = new HashMap<>();
        
        // 添加所有已知的配置键
        String[] allKeys = {
            CRAWLER_ENABLED, CRAWLER_MAX_CONCURRENT, CRAWLER_DEFAULT_COUNT, CRAWLER_TIMEOUT,
            CACHE_ENABLED, CACHE_DEFAULT_TTL,
            MQ_ENABLED, MQ_RETRY_COUNT,
            SYSTEM_MAINTENANCE_MODE, SYSTEM_MAX_UPLOAD_SIZE, SYSTEM_SESSION_TIMEOUT
        };
        
        for (String key : allKeys) {
            allConfigs.put(key, getConfig(key));
        }
        
        // 添加内存缓存中的其他配置
        configCache.forEach((key, value) -> {
            if (!allConfigs.containsKey(key)) {
                allConfigs.put(key, value);
            }
        });
        
        return allConfigs;
    }

    /**
     * 批量设置配置
     */
    public void setConfigs(Map<String, Object> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        
        configs.forEach(this::setConfig);
        logger.info("批量设置配置完成，数量: {}", configs.size());
    }

    /**
     * 重置配置为默认值
     */
    public void resetToDefaults() {
        logger.info("重置所有配置为默认值");
        
        // 清空缓存
        configCache.clear();
        
        // 重新初始化默认配置
        initDefaultConfigs();
    }

    /**
     * 验证配置值
     */
    public boolean validateConfig(String key, Object value) {
        try {
            switch (key) {
                case CRAWLER_MAX_CONCURRENT:
                    int concurrent = Integer.parseInt(String.valueOf(value));
                    return concurrent > 0 && concurrent <= 10;
                    
                case CRAWLER_DEFAULT_COUNT:
                    int count = Integer.parseInt(String.valueOf(value));
                    return count > 0 && count <= 100;
                    
                case CRAWLER_TIMEOUT:
                    int timeout = Integer.parseInt(String.valueOf(value));
                    return timeout > 0 && timeout <= 300;
                    
                case CACHE_DEFAULT_TTL:
                    int ttl = Integer.parseInt(String.valueOf(value));
                    return ttl > 0 && ttl <= 1440; // 最大24小时
                    
                case MQ_RETRY_COUNT:
                    int retry = Integer.parseInt(String.valueOf(value));
                    return retry >= 0 && retry <= 10;
                    
                case SYSTEM_MAX_UPLOAD_SIZE:
                    int size = Integer.parseInt(String.valueOf(value));
                    return size > 0 && size <= 100; // 最大100MB
                    
                case SYSTEM_SESSION_TIMEOUT:
                    int sessionTimeout = Integer.parseInt(String.valueOf(value));
                    return sessionTimeout > 0 && sessionTimeout <= 1440; // 最大24小时
                    
                default:
                    return true; // 其他配置暂不验证
            }
            
        } catch (Exception e) {
            logger.error("配置验证失败: {} = {}", key, value, e);
            return false;
        }
    }

    /**
     * 获取配置描述信息
     */
    public Map<String, String> getConfigDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        
        descriptions.put(CRAWLER_ENABLED, "是否启用爬虫功能");
        descriptions.put(CRAWLER_MAX_CONCURRENT, "爬虫最大并发数 (1-10)");
        descriptions.put(CRAWLER_DEFAULT_COUNT, "爬虫默认抓取数量 (1-100)");
        descriptions.put(CRAWLER_TIMEOUT, "爬虫超时时间(秒) (1-300)");
        
        descriptions.put(CACHE_ENABLED, "是否启用缓存功能");
        descriptions.put(CACHE_DEFAULT_TTL, "缓存默认过期时间(分钟) (1-1440)");
        
        descriptions.put(MQ_ENABLED, "是否启用消息队列功能");
        descriptions.put(MQ_RETRY_COUNT, "消息队列重试次数 (0-10)");
        
        descriptions.put(SYSTEM_MAINTENANCE_MODE, "是否开启维护模式");
        descriptions.put(SYSTEM_MAX_UPLOAD_SIZE, "最大上传文件大小(MB) (1-100)");
        descriptions.put(SYSTEM_SESSION_TIMEOUT, "会话超时时间(分钟) (1-1440)");
        
        return descriptions;
    }

    // 私有辅助方法

    private void setConfigIfNotExists(String key, Object value) {
        if (getConfig(key) == null) {
            setConfig(key, value);
        }
    }

    private Object getDefaultValue(String key) {
        switch (key) {
            case CRAWLER_ENABLED: return true;
            case CRAWLER_MAX_CONCURRENT: return 3;
            case CRAWLER_DEFAULT_COUNT: return 10;
            case CRAWLER_TIMEOUT: return 30;
            case CACHE_ENABLED: return true;
            case CACHE_DEFAULT_TTL: return 30;
            case MQ_ENABLED: return true;
            case MQ_RETRY_COUNT: return 3;
            case SYSTEM_MAINTENANCE_MODE: return false;
            case SYSTEM_MAX_UPLOAD_SIZE: return 10;
            case SYSTEM_SESSION_TIMEOUT: return 120;
            default: return null;
        }
    }

    // 便捷方法

    public boolean isCrawlerEnabled() {
        return getConfig(CRAWLER_ENABLED, Boolean.class);
    }

    public int getCrawlerMaxConcurrent() {
        return getConfig(CRAWLER_MAX_CONCURRENT, Integer.class);
    }

    public int getCrawlerDefaultCount() {
        return getConfig(CRAWLER_DEFAULT_COUNT, Integer.class);
    }

    public boolean isCacheEnabled() {
        return getConfig(CACHE_ENABLED, Boolean.class);
    }

    public boolean isMqEnabled() {
        return getConfig(MQ_ENABLED, Boolean.class);
    }

    public boolean isMaintenanceMode() {
        return getConfig(SYSTEM_MAINTENANCE_MODE, Boolean.class);
    }
}
