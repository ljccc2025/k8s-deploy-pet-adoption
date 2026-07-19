package com.petadoption.pet.catalog;

enum AdoptionStatus {
  AVAILABLE,
  PENDING,
  ADOPTED;

  static AdoptionStatus from(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("adoptionStatus is required");
    }

    try {
      return AdoptionStatus.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("invalid adoptionStatus: " + value);
    }
  }
}
