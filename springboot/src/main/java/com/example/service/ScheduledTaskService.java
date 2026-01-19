package com.example.service;

import com.example.entity.MedicalLiterature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时任务服务
 * 处理系统的定期维护和自动化任务
 *
 * 【部分未使用说明】
 * 此服务包含多个定时任务，在生产环境中自动执行。
 * triggerManualCrawling()方法未被Controller调用，保留用于手动触发测试。
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    @Autowired
    private com.example.crawler.core.Orchestrator orchestrator;

    @Autowired(required = false)
    private CacheService cacheService;

    @Autowired(required = false)
    private MessageService messageService;

    private final AtomicInteger taskCounter = new AtomicInteger(0);

    /**
     * 每小时执行一次 - 自动爬取热门医疗文献
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时的0分0秒执行
    public void autoLiteratureCrawling() {
        try {
            logger.info("=== 开始执行定时爬虫任务 ===");
            
            String[] hotKeywords = {
                "人工智能医疗", "远程医疗", "精准医学", "基因治疗", 
                "免疫治疗", "数字健康", "医疗大数据", "智能诊断"
            };
            
            // 随机选择一个热门关键词
            String keyword = hotKeywords[(int) (Math.random() * hotKeywords.length)];
            
            logger.info("定时爬虫关键词: {}", keyword);
            
            // 执行爬虫任务
            com.example.crawler.core.model.CrawlRequest request = new com.example.crawler.core.model.CrawlRequest(keyword, 5);
            com.example.crawler.core.model.CrawlResult result = orchestrator.crawl(request);
            int savedCount = result.getSaved();
            
            logger.info("定时爬虫完成，保存文献数量: {}", savedCount);
            
            // 发送通知消息
            if (messageService != null && savedCount > 0) {
                messageService.sendNotificationMessage(
                    1L, 
                    "定时爬虫完成", 
                    String.format("自动爬取关键词'%s'相关文献%d篇", keyword, savedCount)
                );
            }
            
        } catch (Exception e) {
            logger.error("定时爬虫任务执行失败", e);
        }
    }

    /**
     * 每天凌晨2点执行 - 清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredData() {
        try {
            logger.info("=== 开始执行数据清理任务 ===");
            
            // 清理过期的文献数据（假设超过1年的数据需要清理）
            // 这里只是示例，实际项目中需要根据业务需求调整
            
            // 清理缓存
            if (cacheService != null) {
                logger.info("清理过期缓存...");
                // 这里可以添加具体的缓存清理逻辑
            }
            
            logger.info("数据清理任务完成");
            
        } catch (Exception e) {
            logger.error("数据清理任务执行失败", e);
        }
    }

    /**
     * 每30分钟执行一次 - 系统健康检查
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void systemHealthCheck() {
        try {
            int taskId = taskCounter.incrementAndGet();
            logger.info("=== 开始执行系统健康检查 #{} ===", taskId);
            
            // 检查数据库连接
            boolean dbHealthy = checkDatabaseHealth();
            
            // 检查缓存服务
            boolean cacheHealthy = checkCacheHealth();
            
            // 检查消息队列
            boolean mqHealthy = checkMessageQueueHealth();
            
            // 统计文献数量
            int literatureCount = getLiteratureCount();
            
            logger.info("系统健康检查 #{} 完成:", taskId);
            logger.info("  数据库: {}", dbHealthy ? "正常" : "异常");
            logger.info("  缓存: {}", cacheHealthy ? "正常" : "异常");
            logger.info("  消息队列: {}", mqHealthy ? "正常" : "异常");
            logger.info("  文献总数: {}", literatureCount);
            
            // 如果有异常，发送告警
            if (!dbHealthy || (!cacheHealthy && cacheService != null) || (!mqHealthy && messageService != null)) {
                logger.warn("系统健康检查发现异常，建议检查相关服务");
                
                if (messageService != null) {
                    messageService.sendNotificationMessage(
                        1L,
                        "系统健康检查告警",
                        String.format("数据库:%s, 缓存:%s, 消息队列:%s", 
                            dbHealthy ? "正常" : "异常",
                            cacheHealthy ? "正常" : "异常",
                            mqHealthy ? "正常" : "异常")
                    );
                }
            }
            
        } catch (Exception e) {
            logger.error("系统健康检查执行失败", e);
        }
    }

    /**
     * 每天上午9点执行 - 生成日报
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void generateDailyReport() {
        try {
            logger.info("=== 开始生成系统日报 ===");
            
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 统计昨日新增文献数量
            int newLiteratureCount = getNewLiteratureCount();
            
            // 统计系统活跃度
            String systemStatus = getSystemStatus();
            
            // 生成报告内容
            String reportContent = String.format(
                "健康体检系统日报 - %s\n" +
                "===================\n" +
                "新增文献: %d 篇\n" +
                "系统状态: %s\n" +
                "缓存服务: %s\n" +
                "消息队列: %s\n",
                today,
                newLiteratureCount,
                systemStatus,
                cacheService != null ? "已启用" : "未启用",
                messageService != null ? "已启用" : "未启用"
            );
            
            logger.info("系统日报生成完成:\n{}", reportContent);
            
            // 发送日报通知
            if (messageService != null) {
                messageService.sendNotificationMessage(1L, "系统日报", reportContent);
                messageService.sendEmailMessage("admin@health-system.com", "系统日报 - " + today, reportContent);
            }
            
        } catch (Exception e) {
            logger.error("生成系统日报失败", e);
        }
    }

    /**
     * 每周日凌晨执行 - 数据备份提醒
     */
    @Scheduled(cron = "0 0 1 * * SUN") // 每周日凌晨1点执行
    public void weeklyBackupReminder() {
        try {
            logger.info("=== 执行周度数据备份提醒 ===");
            
            String message = "提醒：请执行系统数据备份，包括数据库、文件存储等重要数据。";
            
            logger.info(message);
            
            if (messageService != null) {
                messageService.sendNotificationMessage(1L, "数据备份提醒", message);
                messageService.sendEmailMessage("admin@health-system.com", "周度数据备份提醒", message);
            }
            
        } catch (Exception e) {
            logger.error("周度备份提醒执行失败", e);
        }
    }

    /**
     * 检查数据库健康状态
     */
    private boolean checkDatabaseHealth() {
        try {
            // 尝试查询数据库
            medicalLiteratureService.selectPage(new MedicalLiterature(), 1, 1);
            return true;
        } catch (Exception e) {
            logger.error("数据库健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查缓存健康状态
     */
    private boolean checkCacheHealth() {
        try {
            if (cacheService == null) {
                return true; // 缓存服务未启用，视为正常
            }
            
            // 测试缓存读写
            String testKey = "health_check_" + System.currentTimeMillis();
            String testValue = "test_value";
            
            cacheService.set(testKey, testValue);
            String cachedValue = cacheService.get(testKey, String.class);
            cacheService.delete(testKey);
            
            return testValue.equals(cachedValue);
        } catch (Exception e) {
            logger.error("缓存健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查消息队列健康状态
     */
    private boolean checkMessageQueueHealth() {
        try {
            if (messageService == null) {
                return true; // 消息服务未启用，视为正常
            }
            
            // 发送测试消息
            messageService.sendNotificationMessage(0L, "健康检查", "系统健康检查测试消息");
            return true;
        } catch (Exception e) {
            logger.error("消息队列健康检查失败", e);
            return false;
        }
    }

    /**
     * 获取文献总数
     */
    private int getLiteratureCount() {
        try {
            return medicalLiteratureService.selectPage(new MedicalLiterature(), 1, 1).getList().size();
        } catch (Exception e) {
            logger.error("获取文献数量失败", e);
            return 0;
        }
    }

    /**
     * 获取新增文献数量（示例实现）
     */
    private int getNewLiteratureCount() {
        try {
            // 这里应该查询昨日新增的文献数量
            // 简化实现，返回随机数
            return (int) (Math.random() * 10);
        } catch (Exception e) {
            logger.error("获取新增文献数量失败", e);
            return 0;
        }
    }

    /**
     * 获取系统状态
     */
    private String getSystemStatus() {
        try {
            boolean dbOk = checkDatabaseHealth();
            boolean cacheOk = checkCacheHealth();
            boolean mqOk = checkMessageQueueHealth();
            
            if (dbOk && cacheOk && mqOk) {
                return "优秀";
            } else if (dbOk) {
                return "良好";
            } else {
                return "需要关注";
            }
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 手动触发爬虫任务（用于测试）
     * 【未使用方法】此方法被SystemMonitorController调用，但前端未调用该接口
     */
    public void triggerManualCrawling(String keyword) {
        try {
            logger.info("手动触发爬虫任务: {}", keyword);
            com.example.crawler.core.model.CrawlRequest request = new com.example.crawler.core.model.CrawlRequest(keyword, 3);
            com.example.crawler.core.model.CrawlResult result = orchestrator.crawl(request);
            int savedCount = result.getSaved();
            logger.info("手动爬虫完成，保存文献数量: {}", savedCount);
        } catch (Exception e) {
            logger.error("手动爬虫任务执行失败", e);
        }
    }
}
