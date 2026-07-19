package com.petadoption.adoption.application;

import java.time.LocalDateTime;
import java.util.UUID;

record AdoptionApplicationResponse(
    UUID id,
    UUID petId,
    UUID userId,
    String reason,
    String experience,
    AdoptionApplicationStatus status,
    UUID reviewerId,
    String reviewComment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  static AdoptionApplicationResponse from(AdoptionApplication application) {
    return new AdoptionApplicationResponse(
        application.id(),
        application.petId(),
        application.userId(),
        application.reason(),
        application.experience(),
        application.status(),
        application.reviewerId(),
        application.reviewComment(),
        application.createdAt(),
        application.updatedAt());
  }
}
