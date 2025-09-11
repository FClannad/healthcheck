package com.example.service;

import com.example.common.config.RabbitMQConditionalConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息服务类
 * 处理RabbitMQ消息的发送和接收
 */
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送体检报告生成消息
     */
    public void sendReportGenerationMessage(Long orderId, Long userId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("orderId", orderId);
            message.put("userId", userId);
            message.put("timestamp", LocalDateTime.now().toString());
            message.put("type", "REPORT_GENERATION");

            rabbitTemplate.convertAndSend(
                RabbitMQConditionalConfig.HEALTH_EXCHANGE,
                RabbitMQConditionalConfig.REPORT_ROUTING_KEY,
                message
            );

            logger.info("发送体检报告生成消息成功 - 订单ID: {}, 用户ID: {}", orderId, userId);
        } catch (Exception e) {
            logger.error("发送体检报告生成消息失败", e);
        }
    }

    /**
     * 发送系统通知消息
     */
    public void sendNotificationMessage(Long userId, String title, String content) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", LocalDateTime.now().toString());
            message.put("type", "NOTIFICATION");

            rabbitTemplate.convertAndSend(
                RabbitMQConditionalConfig.HEALTH_EXCHANGE,
                RabbitMQConditionalConfig.NOTIFICATION_ROUTING_KEY,
                message
            );

            logger.info("发送系统通知消息成功 - 用户ID: {}, 标题: {}", userId, title);
        } catch (Exception e) {
            logger.error("发送系统通知消息失败", e);
        }
    }

    /**
     * 发送邮件消息
     */
    public void sendEmailMessage(String to, String subject, String body) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("to", to);
            message.put("subject", subject);
            message.put("body", body);
            message.put("timestamp", LocalDateTime.now().toString());
            message.put("type", "EMAIL");

            rabbitTemplate.convertAndSend(
                RabbitMQConditionalConfig.HEALTH_EXCHANGE,
                RabbitMQConditionalConfig.EMAIL_ROUTING_KEY,
                message
            );

            logger.info("发送邮件消息成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (Exception e) {
            logger.error("发送邮件消息失败", e);
        }
    }

    /**
     * 发送爬虫任务消息
     */
    public void sendCrawlerMessage(String keyword, int count, String source) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("keyword", keyword);
            message.put("count", count);
            message.put("source", source);
            message.put("timestamp", LocalDateTime.now().toString());
            message.put("type", "CRAWLER_TASK");

            rabbitTemplate.convertAndSend(
                RabbitMQConditionalConfig.HEALTH_EXCHANGE,
                RabbitMQConditionalConfig.CRAWLER_ROUTING_KEY,
                message
            );

            logger.info("发送爬虫任务消息成功 - 关键词: {}, 数量: {}, 来源: {}", keyword, count, source);
        } catch (Exception e) {
            logger.error("发送爬虫任务消息失败", e);
        }
    }

    /**
     * 处理体检报告生成消息
     */
    @RabbitListener(queues = RabbitMQConditionalConfig.REPORT_GENERATION_QUEUE)
    public void handleReportGenerationMessage(
            @Payload Map<String, Object> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) {
        
        try {
            logger.info("收到体检报告生成消息: {}", message);
            
            Long orderId = Long.valueOf(message.get("orderId").toString());
            Long userId = Long.valueOf(message.get("userId").toString());
            
            // 模拟报告生成处理
            Thread.sleep(2000); // 模拟处理时间
            
            logger.info("体检报告生成完成 - 订单ID: {}, 用户ID: {}", orderId, userId);
            
            // 发送完成通知
            sendNotificationMessage(userId, "体检报告已生成", "您的体检报告已经生成完成，请及时查看。");
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            logger.error("处理体检报告生成消息失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                logger.error("拒绝消息失败", ioException);
            }
        }
    }

    /**
     * 处理系统通知消息
     */
    @RabbitListener(queues = RabbitMQConditionalConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(
            @Payload Map<String, Object> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) {
        
        try {
            logger.info("收到系统通知消息: {}", message);
            
            Long userId = Long.valueOf(message.get("userId").toString());
            String title = message.get("title").toString();
            String content = message.get("content").toString();
            
            // 模拟通知处理
            logger.info("处理系统通知 - 用户ID: {}, 标题: {}, 内容: {}", userId, title, content);
            
            // 这里可以添加实际的通知逻辑，如推送到前端、短信通知等
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            logger.error("处理系统通知消息失败", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                logger.error("拒绝消息失败", ioException);
            }
        }
    }

    /**
     * 处理邮件发送消息
     */
    @RabbitListener(queues = RabbitMQConditionalConfig.EMAIL_QUEUE)
    public void handleEmailMessage(
            @Payload Map<String, Object> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) {
        
        try {
            logger.info("收到邮件发送消息: {}", message);
            
            String to = message.get("to").toString();
            String subject = message.get("subject").toString();
            String body = message.get("body").toString();
            
            // 模拟邮件发送
            logger.info("发送邮件 - 收件人: {}, 主题: {}", to, subject);
            
            // 这里可以添加实际的邮件发送逻辑
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            logger.error("处理邮件发送消息失败", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                logger.error("拒绝消息失败", ioException);
            }
        }
    }

    /**
     * 处理爬虫任务消息
     */
    @RabbitListener(queues = RabbitMQConditionalConfig.CRAWLER_QUEUE)
    public void handleCrawlerMessage(
            @Payload Map<String, Object> message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) {
        
        try {
            logger.info("收到爬虫任务消息: {}", message);
            
            String keyword = message.get("keyword").toString();
            int count = Integer.parseInt(message.get("count").toString());
            String source = message.get("source").toString();
            
            // 模拟爬虫任务处理
            logger.info("执行爬虫任务 - 关键词: {}, 数量: {}, 来源: {}", keyword, count, source);
            
            // 这里可以调用实际的爬虫服务
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            logger.error("处理爬虫任务消息失败", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                logger.error("拒绝消息失败", ioException);
            }
        }
    }
}
