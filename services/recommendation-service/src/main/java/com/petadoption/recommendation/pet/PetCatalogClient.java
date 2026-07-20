package com.petadoption.recommendation.pet;

import java.util.List;
import java.util.UUID;

interface PetCatalogClient {
  PetSummary getPet(UUID id);

  List<PetSummary> availablePets();
}
