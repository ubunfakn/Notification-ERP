package com.notification_hub.notif.producer;

import com.notification_hub.notif.config.RabbitMqConfig;
import com.notification_hub.notif.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationMessage message) {

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                message
        );

        log.info(
                "Notification published to RabbitMQ. NotificationId={}",
                message.getNotificationId()
        );
    }
}
