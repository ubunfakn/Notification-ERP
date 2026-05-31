package com.notification_hub.notif.consumer;

import com.notification_hub.notif.config.RabbitMqConfig;
import com.notification_hub.notif.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {

        log.info(
                "Message received from RabbitMQ. NotificationId={}",
                message.getNotificationId()
        );

        // Just testing if the notification is received or not
    }
}