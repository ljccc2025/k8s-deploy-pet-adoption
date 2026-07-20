package com.petadoption.recommendation.pet;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecommendedPetResponse(
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
}
