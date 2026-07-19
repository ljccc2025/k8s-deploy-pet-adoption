package com.petadoption.notification.notice;

import com.petadoption.notification.events.AdoptionEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final Clock clock;

  NotificationService(NotificationRepository notificationRepository, Clock clock) {
    this.notificationRepository = notificationRepository;
    this.clock = clock;
  }

  @Transactional
  public Notification createFrom(AdoptionEvent event) {
    validate(event);
    return notificationRepository.findByEventTypeAndApplicationIdAndUserId(
            event.eventType(),
            event.applicationId(),
            event.userId())
        .orElseGet(() -> notificationRepository.save(Notification.create(
            event.userId(),
            event.applicationId(),
            event.eventType(),
            messageFor(event.eventType()),
            createdAt(event))));
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> listMine(UUID userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(NotificationResponse::from)
        .toList();
  }

  @Transactional
  public NotificationResponse markRead(UUID notificationId, UUID userId) {
    Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
        .orElseThrow(NotificationNotFoundException::new);
    notification.markRead(Instant.now(clock));
    return NotificationResponse.from(notification);
  }

  private Instant createdAt(AdoptionEvent event) {
    return event.occurredAt() == null ? Instant.now(clock) : event.occurredAt();
  }

  private static void validate(AdoptionEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("adoption event is required");
    }
    if (event.eventType() == null || event.eventType().isBlank()) {
      throw new IllegalArgumentException("event type is required");
    }
    if (event.applicationId() == null) {
      throw new IllegalArgumentException("application id is required");
    }
    if (event.userId() == null) {
      throw new IllegalArgumentException("user id is required");
    }
  }

  private static String messageFor(String eventType) {
    return switch (eventType) {
      case "adoption.submitted" -> "你的领养申请已提交，请等待管理员审核。";
      case "adoption.approved" -> "你的领养申请已通过，请留意后续联系。";
      case "adoption.rejected" -> "你的领养申请未通过，可以继续关注其它宠物。";
      case "adoption.cancelled" -> "你的领养申请已取消。";
      default -> "你的领养申请状态已更新。";
    };
  }
}
