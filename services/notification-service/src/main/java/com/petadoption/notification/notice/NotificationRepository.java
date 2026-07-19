package com.petadoption.notification.notice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  Optional<Notification> findByEventTypeAndApplicationIdAndUserId(
      String eventType,
      UUID applicationId,
      UUID userId);

  List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
