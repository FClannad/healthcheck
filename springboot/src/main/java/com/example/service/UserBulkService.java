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
        public int requested;
        public int inserted;
        public long millis;
        public String mode; // single | batch
        public int batchSize;
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
        r.requested = users.size();
        r.inserted = ok;
        r.millis = System.currentTimeMillis() - start;
        r.mode = "single";
        r.batchSize = 1;
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
                    try { userMapper.insert(u); totalOk++; } catch (Exception ignore) {}
                }
            }
        }
        BulkResult r = new BulkResult();
        r.requested = users.size();
        r.inserted = totalOk;
        r.millis = System.currentTimeMillis() - start;
        r.mode = "batch";
        r.batchSize = batchSize;
        return r;
    }
}

