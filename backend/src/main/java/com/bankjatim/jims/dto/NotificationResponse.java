package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        String priority,
        String link,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        String referenceTransactionType,
        Long referenceTransactionId
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getPriority(),
                notification.getActionUrl(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getReferenceTransactionType(),
                notification.getReferenceTransactionId()
        );
    }
}
