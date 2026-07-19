package com.petadoption.adoption.application;

import com.petadoption.adoption.messaging.AdoptionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "adoption_outbox_events", schema = "adoption_schema")
class AdoptionOutboxEvent {
  @Id
  private UUID id;

  @Column(name = "event_type", nullable = false, length = 128)
  private String eventType;

  @Column(name = "application_id", nullable = false)
  private UUID applicationId;

  @Column(name = "pet_id", nullable = false)
  private UUID petId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "pet_status_update", length = 32)
  private String petStatusUpdate;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  protected AdoptionOutboxEvent() {
  }

  private AdoptionOutboxEvent(
      UUID id,
      String eventType,
      UUID applicationId,
      UUID petId,
      UUID userId,
      String petStatusUpdate,
      Instant occurredAt) {
    this.id = id;
    this.eventType = eventType;
    this.applicationId = applicationId;
    this.petId = petId;
    this.userId = userId;
    this.petStatusUpdate = petStatusUpdate;
    this.occurredAt = occurredAt;
  }

  static AdoptionOutboxEvent create(String eventType, AdoptionApplication application, String petStatusUpdate) {
    return new AdoptionOutboxEvent(
        UUID.randomUUID(),
        eventType,
        application.id(),
        application.petId(),
        application.userId(),
        petStatusUpdate,
        Instant.now());
  }

  AdoptionEvent toAdoptionEvent() {
    return new AdoptionEvent(eventType, applicationId, petId, userId, occurredAt);
  }

  void markProcessed(Instant processedAt) {
    this.processedAt = processedAt;
    this.errorMessage = null;
  }

  void markFailed(Exception exception) {
    this.errorMessage = exception.getMessage();
  }

  UUID petId() {
    return petId;
  }

  String petStatusUpdate() {
    return petStatusUpdate;
  }
}
