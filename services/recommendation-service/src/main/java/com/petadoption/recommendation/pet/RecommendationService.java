package com.petadoption.recommendation.pet;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class RecommendationService {
  private final PetCatalogClient petCatalogClient;
  private final RecommendationRuleService recommendationRuleService;

  RecommendationService(PetCatalogClient petCatalogClient, RecommendationRuleService recommendationRuleService) {
    this.petCatalogClient = petCatalogClient;
    this.recommendationRuleService = recommendationRuleService;
  }

  List<RecommendedPetResponse> recommendPets(UUID petId) {
    PetSummary current = petCatalogClient.getPet(petId);
    return recommendationRuleService.recommend(current, petCatalogClient.availablePets());
  }
}
