package com.notification_hub.notif.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;

@Configuration
@Slf4j
public class RabbitTemplateConfig {

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        log.info("Jackson2JsonConverter Bean Initialized");
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter) {
        log.info("RabbitTemplate Bean Initialized");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}
