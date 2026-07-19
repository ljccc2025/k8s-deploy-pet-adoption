package com.petadoption.adoption.application;

import java.util.UUID;

public class PetCatalogNotFoundException extends RuntimeException {
  public PetCatalogNotFoundException(UUID petId) {
    super("pet not found: " + petId);
  }
}
