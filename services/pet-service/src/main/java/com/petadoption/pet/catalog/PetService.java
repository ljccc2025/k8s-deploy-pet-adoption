package com.petadoption.pet.catalog;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PetService {
  private final PetRepository petRepository;
  private final Clock clock;

  PetService(PetRepository petRepository, Clock clock) {
    this.petRepository = petRepository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  List<PetResponse> list(String status) {
    List<Pet> pets = status == null || status.isBlank()
        ? petRepository.findAllByOrderByCreatedAtDesc()
        : petRepository.findByAdoptionStatusOrderByCreatedAtDesc(AdoptionStatus.from(status));
    return pets.stream()
        .map(PetResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  PetResponse get(UUID id) {
    return petRepository.findById(id)
        .map(PetResponse::from)
        .orElseThrow(PetNotFoundException::new);
  }

  @Transactional
  PetResponse create(PetRequest request) {
    LocalDateTime now = LocalDateTime.now(clock);
    Pet pet = new Pet(UUID.randomUUID(), now);
    apply(pet, request, now);
    return PetResponse.from(petRepository.save(pet));
  }

  @Transactional
  PetResponse update(UUID id, PetRequest request) {
    Pet pet = petRepository.findById(id)
        .orElseThrow(PetNotFoundException::new);
    apply(pet, request, LocalDateTime.now(clock));
    return PetResponse.from(petRepository.save(pet));
  }

  @Transactional
  PetResponse updateAdoptionStatus(UUID id, UpdateAdoptionStatusRequest request) {
    Pet pet = petRepository.findById(id)
        .orElseThrow(PetNotFoundException::new);
    pet.updateAdoptionStatus(AdoptionStatus.from(request.adoptionStatus()), LocalDateTime.now(clock));
    return PetResponse.from(petRepository.save(pet));
  }

  private void apply(Pet pet, PetRequest request, LocalDateTime updatedAt) {
    pet.update(
        requireText(request.name(), "name"),
        requireText(request.type(), "type"),
        requireText(request.gender(), "gender"),
        requireAgeMonths(request.ageMonths()),
        requireText(request.city(), "city"),
        requireText(request.healthStatus(), "healthStatus"),
        AdoptionStatus.from(request.adoptionStatus()),
        trimOptional(request.imageUrl()),
        requireText(request.description(), "description"),
        updatedAt);
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value.trim();
  }

  private static int requireAgeMonths(Integer ageMonths) {
    if (ageMonths == null) {
      throw new IllegalArgumentException("ageMonths is required");
    }
    if (ageMonths < 0) {
      throw new IllegalArgumentException("ageMonths must be greater than or equal to 0");
    }
    return ageMonths;
  }

  private static String trimOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
