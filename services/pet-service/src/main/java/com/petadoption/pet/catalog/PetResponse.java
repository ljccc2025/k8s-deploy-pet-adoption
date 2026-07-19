package com.petadoption.pet.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

public record PetResponse(
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
  static PetResponse from(Pet pet) {
    return new PetResponse(
        pet.id(),
        pet.name(),
        pet.type(),
        pet.gender(),
        pet.ageMonths(),
        pet.city(),
        pet.healthStatus(),
        pet.adoptionStatus().name(),
        pet.imageUrl(),
        pet.description(),
        pet.createdAt(),
        pet.updatedAt());
  }
}
