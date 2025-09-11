package com.example.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RabbitMQ条件配置类
 * 当spring.rabbitmq.enabled=true时才启用RabbitMQ相关配置
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConditionalConfig {

    // 队列名称常量
    public static final String REPORT_GENERATION_QUEUE = "report.generation.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String CRAWLER_QUEUE = "crawler.queue";

    // 交换机名称常量
    public static final String HEALTH_EXCHANGE = "health.exchange";
    public static final String DLX_EXCHANGE = "health.dlx.exchange";

    // 路由键常量
    public static final String REPORT_ROUTING_KEY = "health.report";
    public static final String NOTIFICATION_ROUTING_KEY = "health.notification";
    public static final String EMAIL_ROUTING_KEY = "health.email";
    public static final String CRAWLER_ROUTING_KEY = "health.crawler";

    /**
     * 配置RabbitTemplate
     */
    /**
     * 消息转换器 - 使用JSON格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
    public RabbitTemplate conditionalRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置消息转换器
        rabbitTemplate.setMessageConverter(messageConverter());

        // 设置消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.err.println("消息发送失败: " + correlationData + ", 原因: " + cause);
            }
        });

        // 设置消息返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage() +
                ", 退回码: " + returned.getReplyCode() +
                ", 退回原因: " + returned.getReplyText());
        });

        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂配置
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // 设置并发消费者数量
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);

        // 设置预取数量
        factory.setPrefetchCount(1);

        // 启用手动确认
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return factory;
    }

    /**
     * 主交换机 - Direct类型
     */
    @Bean
    public DirectExchange healthExchange() {
        return ExchangeBuilder
                .directExchange(HEALTH_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 体检报告生成队列
     */
    @Bean
    public Queue reportGenerationQueue() {
        return QueueBuilder
                .durable(REPORT_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.report")
                .withArgument("x-message-ttl", 300000) // 5分钟TTL
                .build();
    }

    /**
     * 系统通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.notification")
                .withArgument("x-message-ttl", 600000) // 10分钟TTL
                .build();
    }

    /**
     * 邮件发送队列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.email")
                .withArgument("x-message-ttl", 1800000) // 30分钟TTL
                .build();
    }

    /**
     * 爬虫任务队列
     */
    @Bean
    public Queue crawlerQueue() {
        return QueueBuilder
                .durable(CRAWLER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.crawler")
                .withArgument("x-message-ttl", 900000) // 15分钟TTL
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable("health.dlx.queue")
                .build();
    }

    /**
     * 绑定体检报告队列到交换机
     */
    @Bean
    public Binding reportGenerationBinding() {
        return BindingBuilder
                .bind(reportGenerationQueue())
                .to(healthExchange())
                .with(REPORT_ROUTING_KEY);
    }

    /**
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(healthExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 绑定邮件队列到交换机
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(healthExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    /**
     * 绑定爬虫队列到交换机
     */
    @Bean
    public Binding crawlerBinding() {
        return BindingBuilder
                .bind(crawlerQueue())
                .to(healthExchange())
                .with(CRAWLER_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(dlxExchange())
                .with("dlx.#");
    }
}