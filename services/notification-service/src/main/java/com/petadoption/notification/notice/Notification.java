package com.petadoption.notification.notice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID applicationId;

  @Column(nullable = false, length = 100)
  private String eventType;

  @Column(nullable = false, length = 500)
  private String message;

  private Instant readAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected Notification() {
  }

  private Notification(
      UUID id,
      UUID userId,
      UUID applicationId,
      String eventType,
      String message,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.message = message;
    this.createdAt = createdAt;
  }

  static Notification create(
      UUID userId,
      UUID applicationId,
      String eventType,
      String message,
      Instant createdAt) {
    return new Notification(UUID.randomUUID(), userId, applicationId, eventType, message, createdAt);
  }

  void markRead(Instant readAt) {
    if (this.readAt == null) {
      this.readAt = readAt;
    }
  }

  public UUID id() {
    return id;
  }

  public UUID userId() {
    return userId;
  }

  public UUID applicationId() {
    return applicationId;
  }

  public String eventType() {
    return eventType;
  }

  public String message() {
    return message;
  }

  public Instant readAt() {
    return readAt;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
