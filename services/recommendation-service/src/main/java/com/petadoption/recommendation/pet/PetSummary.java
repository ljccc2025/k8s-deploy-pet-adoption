package com.petadoption.recommendation.pet;

import java.time.LocalDateTime;
import java.util.UUID;

record PetSummary(
    UUID id,
    String name,
    String type,
    String gender,
    int ageMonths,
    String city,
    String healthStatus,
    String adoptionStatus,
    String imageUrl,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  RecommendedPetResponse toResponse() {
    return new RecommendedPetResponse(
        id,
        name,
        type,
        gender,
        ageMonths,
        city,
        healthStatus,
        adoptionStatus,
        imageUrl,
        description,
        createdAt,
        updatedAt);
  }
}
