package com.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 解决并发场景下的数据一致性问题，防止缓存击穿、穿透等问题
 */
@Component
public class RedisDistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 分布式锁的键前缀
     */
    private static final String LOCK_PREFIX = "distributed_lock:";

    /**
     * 默认锁过期时间（秒）
     */
    private static final int DEFAULT_EXPIRE_TIME = 30;

    /**
     * Lua脚本：原子性释放锁
     */
    private static final String UNLOCK_LUA_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else " +
        "return 0 " +
        "end";

    /**
     * 获取分布式锁
     * 
     * @param lockKey 锁的标识
     * @param expireTime 过期时间（秒）
     * @return 锁标识（用于释放锁）
     */
    public String tryLock(String lockKey, int expireTime) {
        String lockValue = UUID.randomUUID().toString().replace("-", "");
        String actualKey = LOCK_PREFIX + lockKey;
        
        try {
            // 使用SET命令的NX和EX参数，原子性地设置锁
            Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(actualKey, lockValue, expireTime, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                logger.debug("成功获取分布式锁: {}, 值: {}", actualKey, lockValue);
                return lockValue;
            } else {
                logger.debug("获取分布式锁失败: {}", actualKey);
                return null;
            }
        } catch (Exception e) {
            logger.error("获取分布式锁异常: {}", actualKey, e);
            return null;
        }
    }

    /**
     * 获取分布式锁（使用默认过期时间）
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁的标识
     * @param lockValue 锁的值（用于验证）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        String actualKey = LOCK_PREFIX + lockKey;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_LUA_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = stringRedisTemplate.execute(script, 
                Collections.singletonList(actualKey), lockValue);
            
            boolean success = result != null && result == 1L;
            
            if (success) {
                logger.debug("成功释放分布式锁: {}", actualKey);
            } else {
                logger.warn("释放分布式锁失败，可能锁已过期或不存在: {}", actualKey);
            }
            
            return success;
        } catch (Exception e) {
            logger.error("释放分布式锁异常: {}", actualKey, e);
            return false;
        }
    }

    /**
     * 尝试获取锁并执行业务逻辑
     * 
     * @param lockKey 锁标识
     * @param expireTime 过期时间
     * @param business 业务逻辑
     * @return 执行结果
     */
    public <T> T executeWithLock(String lockKey, int expireTime, 
                                 LockCallback<T> business) {
        String lockValue = tryLock(lockKey, expireTime);
        if (lockValue == null) {
            logger.warn("获取分布式锁失败，无法执行业务逻辑: {}", lockKey);
            return null;
        }
        
        try {
            return business.doInLock();
        } catch (Exception e) {
            logger.error("执行业务逻辑异常: {}", lockKey, e);
            throw new RuntimeException("执行业务逻辑失败", e);
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 尝试获取锁并执行业务逻辑（使用默认过期时间）
     */
    public <T> T executeWithLock(String lockKey, LockCallback<T> business) {
        return executeWithLock(lockKey, DEFAULT_EXPIRE_TIME, business);
    }

    /**
     * 检查锁是否存在
     */
    public boolean isLockExist(String lockKey) {
        String actualKey = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(actualKey));
    }

    /**
     * 获取锁的剩余过期时间（秒）
     */
    public long getLockTtl(String lockKey) {
        String actualKey = LOCK_PREFIX + lockKey;
        return stringRedisTemplate.getExpire(actualKey, TimeUnit.SECONDS);
    }

    /**
     * 业务逻辑回调接口
     */
    @FunctionalInterface
    public interface LockCallback<T> {
        T doInLock() throws Exception;
    }
}