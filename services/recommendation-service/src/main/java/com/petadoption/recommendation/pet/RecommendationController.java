package com.petadoption.recommendation.pet;

import com.petadoption.common.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RecommendationController {
  private final RecommendationService recommendationService;

  RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @GetMapping("/api/v1/recommendations/pets")
  ApiResponse<List<RecommendedPetResponse>> recommendedPets(@RequestParam("petId") UUID petId) {
    return ApiResponse.success(recommendationService.recommendPets(petId));
  }
}
