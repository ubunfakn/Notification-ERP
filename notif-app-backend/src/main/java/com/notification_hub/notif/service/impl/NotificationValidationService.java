package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.CreateNotificationRequest;
import com.notification_hub.notif.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationValidationService {

    private final NotificationRepo notificationRepo;

    public void validateDuplicateNotification(CreateNotificationRequest request) {

        notificationRepo.findFirstByUserIdOrderByCreatedAtDesc(request.getUserId())
                .ifPresent(existing -> {

                    log.info("Notification found for user {}", request.getUserId());
                    LocalDateTime now = LocalDateTime.now();

                    boolean sameType =
                            existing.getType() != null
                                    && request.getType() != null
                                    && existing.getType().equals(request.getType());
                    log.info("Notification is of same type {}" , sameType);

                    boolean sameMessage =
                            existing.getMessage() != null
                                    && request.getMessage() != null
                                    && existing.getMessage().equalsIgnoreCase(request.getMessage());
                    log.info("Notification contains same message {}" , sameMessage);

                    boolean withinFiveMinutes =
                            existing.getCreatedAt() != null
                                    && !now.isAfter(existing.getCreatedAt().plusMinutes(5));
                    log.info("Notification created within 5 minutes {}" , withinFiveMinutes);

                    if (sameType && sameMessage && withinFiveMinutes) {
                        throw new IllegalArgumentException(
                                "Duplicate notification restriction violated. " +
                                        "User cannot create same type, same message, or create another notification within 5 minutes.");
                    }
                });

        log.info("Checking if message contains 3 same words");
        validateRepeatedWords(request.getMessage());
    }

    private void validateRepeatedWords(String message) {

        if (message == null || message.isBlank()) {
            return;
        }

        log.info("Using map finding duplicate elements");
        // Time Complexity O(N)
        Map<String, Integer> wordCount = new HashMap<>();

        //Splitting words by spaces
        for (String word : message.toLowerCase().split("\\s+")) {
            int count = wordCount.getOrDefault(word, 0) + 1;

            // If same word then storing the count and if count is equal to 3 then throw error
            if (count >= 3) {
                throw new IllegalArgumentException(
                        "Message contains a word repeated 3 or more times: " + word);
            }

            wordCount.put(word, count);
        }
        log.info("Duplicate check successful");
    }
}
