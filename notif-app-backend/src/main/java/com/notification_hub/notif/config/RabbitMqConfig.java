package com.notification_hub.notif.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;

@Configuration
@Slf4j
public class RabbitMqConfig {

    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";

    @Bean
    public Queue notificationQueue() {
        log.info("Notification Queue Bean initialized");
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        log.info("Notification Exchange Bean initialized");
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding binding(
            Queue notificationQueue,
            TopicExchange notificationExchange) {
        log.info("Binding done and bean initialized");
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }
}
