package com.example.service;

import cn.hutool.core.util.RandomUtil;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserBulkService {

    private static final Logger log = LoggerFactory.getLogger(UserBulkService.class);

    @Autowired
    private UserMapper userMapper;

    public static class BulkResult {
        private int requested;
        private int inserted;
        private long millis;
        private String mode; // single | batch
        private int batchSize;

        public int getRequested() {
            return requested;
        }

        public void setRequested(int requested) {
            this.requested = requested;
        }

        public int getInserted() {
            return inserted;
        }

        public void setInserted(int inserted) {
            this.inserted = inserted;
        }

        public long getMillis() {
            return millis;
        }

        public void setMillis(long millis) {
            this.millis = millis;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    // 生成模拟用户数据
    public List<User> generateUsers(int count) {
        List<User> list = new ArrayList<>(count);
        Set<String> usernames = new HashSet<>(count * 2);
        for (int i = 0; i < count; i++) {
            User u = new User();
            // 避免唯一键冲突，生成高熵用户名
            String username;
            do {
                username = "user_" + RandomUtil.randomString(12) + "_" + i;
            } while (!usernames.add(username));
            u.setUsername(username);
            u.setPassword("123456");
            u.setName("用户" + i);
            u.setPhone("1" + RandomUtil.randomNumbers(10));
            u.setEmail(username + "@example.com");
            u.setAvatar(null);
            u.setRole("USER");
            list.add(u);
        }
        return list;
    }

    // 单条插入（逐条）
    public BulkResult insertSingle(List<User> users) {
        long start = System.currentTimeMillis();
        int ok = 0;
        for (User u : users) {
            try {
                userMapper.insert(u);
                ok++;
            } catch (Exception e) {
                log.warn("Single insert failed for {}: {}", u.getUsername(), e.getMessage());
            }
        }
        BulkResult r = new BulkResult();
        r.setRequested(users.size());
        r.setInserted(ok);
        r.setMillis(System.currentTimeMillis() - start);
        r.setMode("single");
        r.setBatchSize(1);
        return r;
    }

    // 批量插入（多VALUES，分片）
    public BulkResult insertBatch(List<User> users, int batchSize) {
        long start = System.currentTimeMillis();
        int totalOk = 0;
        int n = users.size();
        for (int i = 0; i < n; i += batchSize) {
            int end = Math.min(i + batchSize, n);
            List<User> slice = users.subList(i, end);
            try {
                int inserted = userMapper.batchInsert(slice);
                totalOk += inserted;
            } catch (Exception e) {
                log.warn("Batch insert failed for slice {}-{}: {}", i, end, e.getMessage());
                // 退化为单条以定位异常数据
                for (User u : slice) {
                    try {
                        userMapper.insert(u);
                        totalOk++;
                    } catch (Exception fallbackException) {
                        log.debug("Individual insert also failed for user {}: {}", u.getUsername(), fallbackException.getMessage());
                    }
                }
            }
        }
        BulkResult r = new BulkResult();
        r.setRequested(users.size());
        r.setInserted(totalOk);
        r.setMillis(System.currentTimeMillis() - start);
        r.setMode("batch");
        r.setBatchSize(batchSize);
        return r;
    }
}

