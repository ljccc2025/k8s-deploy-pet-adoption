package com.petadoption.adoption.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adoption_applications", schema = "adoption_schema")
class AdoptionApplication {
  @Id
  private UUID id;

  @Column(name = "pet_id", nullable = false)
  private UUID petId;

  @Column(name = "active_pet_id")
  private UUID activePetId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String reason;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String experience;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AdoptionApplicationStatus status;

  @Column(name = "reviewer_id")
  private UUID reviewerId;

  @Column(name = "review_comment", columnDefinition = "TEXT")
  private String reviewComment;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected AdoptionApplication() {
  }

  AdoptionApplication(UUID id, UUID petId, UUID userId, String reason, String experience, LocalDateTime now) {
    this.id = id;
    this.petId = petId;
    this.activePetId = petId;
    this.userId = userId;
    this.reason = reason;
    this.experience = experience;
    this.status = AdoptionApplicationStatus.SUBMITTED;
    this.createdAt = now;
    this.updatedAt = now;
  }

  void approve(UUID reviewerId, LocalDateTime now) {
    requireSubmitted();
    this.status = AdoptionApplicationStatus.APPROVED;
    this.reviewerId = reviewerId;
    this.updatedAt = now;
  }

  void reject(UUID reviewerId, String reviewComment, LocalDateTime now) {
    requireSubmitted();
    this.status = AdoptionApplicationStatus.REJECTED;
    this.reviewerId = reviewerId;
    this.reviewComment = reviewComment;
    this.activePetId = null;
    this.updatedAt = now;
  }

  void cancel(UUID actorId, LocalDateTime now) {
    if (!userId.equals(actorId)) {
      throw new AdoptionApplicationNotFoundException();
    }
    requireSubmitted();
    this.status = AdoptionApplicationStatus.CANCELLED;
    this.activePetId = null;
    this.updatedAt = now;
  }

  private void requireSubmitted() {
    if (status != AdoptionApplicationStatus.SUBMITTED) {
      throw new InvalidAdoptionStateException("adoption application cannot transition from " + status);
    }
  }

  UUID id() {
    return id;
  }

  UUID petId() {
    return petId;
  }

  UUID userId() {
    return userId;
  }

  String reason() {
    return reason;
  }

  String experience() {
    return experience;
  }

  AdoptionApplicationStatus status() {
    return status;
  }

  UUID reviewerId() {
    return reviewerId;
  }

  String reviewComment() {
    return reviewComment;
  }

  LocalDateTime createdAt() {
    return createdAt;
  }

  LocalDateTime updatedAt() {
    return updatedAt;
  }
}
