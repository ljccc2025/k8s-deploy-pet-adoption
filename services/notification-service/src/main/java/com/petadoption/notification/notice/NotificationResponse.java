package com.petadoption.notification.notice;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID userId,
    UUID applicationId,
    String eventType,
    String message,
    Instant readAt,
    Instant createdAt) {
  static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.id(),
        notification.userId(),
        notification.applicationId(),
        notification.eventType(),
        notification.message(),
        notification.readAt(),
        notification.createdAt());
  }
}
