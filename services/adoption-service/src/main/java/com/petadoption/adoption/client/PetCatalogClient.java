package com.petadoption.adoption.client;

import java.util.UUID;

public interface PetCatalogClient {
  void requireAvailable(UUID petId);

  void updateAdoptionStatus(UUID petId, String adoptionStatus);
}
