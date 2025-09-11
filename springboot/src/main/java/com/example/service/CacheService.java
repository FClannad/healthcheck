package com.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务类
 * 提供统一的缓存操作接口，支持Redis
 * 遵循阿里巴巴Java开发规范
 * 
 * @author Medical System
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@ConditionalOnProperty(name = "spring.data.redis.host")
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "health:";
    private static final String USER_PREFIX = CACHE_PREFIX + "user:";
    private static final String LITERATURE_PREFIX = CACHE_PREFIX + "literature:";
    private static final String EXAM_PREFIX = CACHE_PREFIX + "exam:";
    private static final String PACKAGE_PREFIX = CACHE_PREFIX + "package:";

    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                redisTemplate.opsForValue().set(fullKey, value, timeout, unit);
                logger.debug("缓存设置成功: {}", fullKey);
            }
        } catch (Exception e) {
            logger.error("设置缓存失败: {}", key, e);
        }
    }

    /**
     * 设置缓存（默认30分钟过期）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                Object value = redisTemplate.opsForValue().get(fullKey);
                logger.debug("缓存获取: {} = {}", fullKey, value != null ? "命中" : "未命中");
                return value;
            }
        } catch (Exception e) {
            logger.error("获取缓存失败: {}", key, e);
        }
        return null;
    }

    /**
     * 获取缓存并转换为指定类型
     * 
     * @param key 缓存键
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null) {
                return null;
            }
            
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            
            // 尝试JSON转换
            if (value instanceof String) {
                return objectMapper.readValue((String) value, clazz);
            }
            
            // 尝试直接转换
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, clazz);
            
        } catch (Exception e) {
            logger.error("获取并转换缓存失败: {}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                redisTemplate.delete(fullKey);
                logger.debug("缓存删除: {}", fullKey);
            }
        } catch (Exception e) {
            logger.error("删除缓存失败: {}", key, e);
        }
    }

    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
            }
        } catch (Exception e) {
            logger.error("检查缓存存在性失败: {}", key, e);
        }
        return false;
    }

    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                return Boolean.TRUE.equals(redisTemplate.expire(fullKey, timeout, unit));
            }
        } catch (Exception e) {
            logger.error("设置缓存过期时间失败: {}", key, e);
        }
        return false;
    }

    /**
     * 获取缓存剩余过期时间
     * 
     * @param key 缓存键
     * @param unit 时间单位
     * @return 剩余时间
     */
    public long getExpire(String key, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                String fullKey = CACHE_PREFIX + key;
                Long expire = redisTemplate.getExpire(fullKey, unit);
                return expire != null ? expire : -1;
            }
        } catch (Exception e) {
            logger.error("获取缓存过期时间失败: {}", key, e);
        }
        return -1;
    }

    /**
     * 缓存用户信息
     * 
     * @param userId 用户ID
     * @param userInfo 用户信息
     */
    public void cacheUserInfo(String userId, Object userInfo) {
        set(USER_PREFIX + userId, userInfo, 1, TimeUnit.HOURS);
    }

    /**
     * 获取用户缓存信息
     * 
     * @param userId 用户ID
     * @param clazz 用户信息类型
     * @param <T> 泛型类型
     * @return 用户信息
     */
    public <T> T getUserInfo(String userId, Class<T> clazz) {
        return get(USER_PREFIX + userId, clazz);
    }

    /**
     * 缓存文献信息
     * 
     * @param literatureId 文献ID
     * @param literature 文献信息
     */
    public void cacheLiterature(String literatureId, Object literature) {
        set(LITERATURE_PREFIX + literatureId, literature, 2, TimeUnit.HOURS);
    }

    /**
     * 获取文献缓存信息
     * 
     * @param literatureId 文献ID
     * @param clazz 文献信息类型
     * @param <T> 泛型类型
     * @return 文献信息
     */
    public <T> T getLiterature(String literatureId, Class<T> clazz) {
        return get(LITERATURE_PREFIX + literatureId, clazz);
    }

    /**
     * 缓存体检套餐信息
     * 
     * @param packageId 套餐ID
     * @param packageInfo 套餐信息
     */
    public void cachePackage(String packageId, Object packageInfo) {
        set(PACKAGE_PREFIX + packageId, packageInfo, 4, TimeUnit.HOURS);
    }

    /**
     * 获取体检套餐缓存信息
     * 
     * @param packageId 套餐ID
     * @param clazz 套餐信息类型
     * @param <T> 泛型类型
     * @return 套餐信息
     */
    public <T> T getPackage(String packageId, Class<T> clazz) {
        return get(PACKAGE_PREFIX + packageId, clazz);
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        try {
            if (redisTemplate != null) {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
                logger.info("清除所有缓存成功");
            }
        } catch (Exception e) {
            logger.error("清除所有缓存失败", e);
        }
    }

    /**
     * 清除指定前缀的缓存
     *
     * @param prefix 缓存前缀
     */
    public void clearByPrefix(String prefix) {
        try {
            if (redisTemplate != null) {
                String pattern = CACHE_PREFIX + prefix + "*";
                redisTemplate.delete(redisTemplate.keys(pattern));
                logger.info("清除前缀缓存成功: {}", prefix);
            }
        } catch (Exception e) {
            logger.error("清除前缀缓存失败: {}", prefix, e);
        }
    }

    /**
     * 清除所有缓存（别名方法）
     */
    public void clearAll() {
        clear();
    }

    /**
     * 缓存统计信息
     *
     * @param key 统计键
     * @param statistics 统计数据
     */
    public void cacheStatistics(String key, Object statistics) {
        set("statistics:" + key, statistics, 1, TimeUnit.HOURS);
    }

    /**
     * 获取缓存信息
     *
     * @return 缓存信息
     */
    public Object getCacheInfo() {
        try {
            if (redisTemplate != null) {
                // 获取Redis信息
                java.util.Map<String, Object> info = new java.util.HashMap<>();
                info.put("type", "Redis");
                info.put("status", "connected");

                // 获取数据库大小
                Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
                info.put("keyCount", dbSize != null ? dbSize : 0);

                return info;
            } else {
                java.util.Map<String, Object> info = new java.util.HashMap<>();
                info.put("type", "None");
                info.put("status", "disabled");
                info.put("keyCount", 0);
                return info;
            }
        } catch (Exception e) {
            logger.error("获取缓存信息失败", e);
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("type", "Redis");
            info.put("status", "error");
            info.put("keyCount", 0);
            return info;
        }
    }
}
