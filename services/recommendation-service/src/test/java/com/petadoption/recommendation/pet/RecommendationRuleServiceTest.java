package com.petadoption.recommendation.pet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationRuleServiceTest {
  private final RecommendationRuleService recommendationRuleService = new RecommendationRuleService();

  @Test
  void recommendsSameTypeOrSameCityPetsExcludingCurrentAndPreservingOrder() {
    PetSummary current = pet("current", "CAT", "Shanghai");
    PetSummary sameType = pet("same-type", "CAT", "Beijing");
    PetSummary sameCity = pet("same-city", "DOG", "Shanghai");
    PetSummary unrelated = pet("unrelated", "DOG", "Hangzhou");

    List<RecommendedPetResponse> recommendations = recommendationRuleService.recommend(
        current,
        List.of(current, sameType, sameCity, unrelated));

    assertThat(recommendations)
        .extracting(RecommendedPetResponse::id)
        .containsExactly(sameType.id(), sameCity.id());
  }

  @Test
  void limitsRecommendationsToSixPets() {
    PetSummary current = pet("current", "CAT", "Shanghai");
    List<PetSummary> candidates = List.of(
        current,
        pet("match-1", "CAT", "Beijing"),
        pet("match-2", "CAT", "Beijing"),
        pet("match-3", "CAT", "Beijing"),
        pet("match-4", "CAT", "Beijing"),
        pet("match-5", "CAT", "Beijing"),
        pet("match-6", "CAT", "Beijing"),
        pet("match-7", "CAT", "Beijing"));

    List<RecommendedPetResponse> recommendations = recommendationRuleService.recommend(current, candidates);

    assertThat(recommendations).hasSize(6);
  }

  private static PetSummary pet(String seed, String type, String city) {
    return new PetSummary(
        UUID.nameUUIDFromBytes(seed.getBytes()),
        seed,
        type,
        "FEMALE",
        12,
        city,
        "HEALTHY",
        "AVAILABLE",
        "https://example.com/" + seed + ".jpg",
        seed + " description",
        LocalDateTime.parse("2026-07-19T12:00:00"),
        LocalDateTime.parse("2026-07-19T12:00:00"));
  }
}
