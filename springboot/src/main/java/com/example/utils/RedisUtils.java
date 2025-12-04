package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
 */
@Deprecated
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#set(String, Object, long, TimeUnit)} 替代
     */
    @Deprecated
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#set(String, Object, long, TimeUnit)} 替代
     */
    @Deprecated
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#get(String)} 替代
     */
    @Deprecated
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#delete(String)} 替代
     */
    @Deprecated
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#exists(String)} 替代
     */
    @Deprecated
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#expire(String, long, TimeUnit)} 替代
     */
    @Deprecated
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService#getExpire(String, TimeUnit)} 替代
     */
    @Deprecated
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * Hash设置
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
     */
    @Deprecated
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Hash获取
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
     */
    @Deprecated
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Hash删除
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
     */
    @Deprecated
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 递增
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
     */
    @Deprecated
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     * @deprecated 已废弃，请使用 {@link com.example.service.CacheService} 替代
     */
    @Deprecated
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}