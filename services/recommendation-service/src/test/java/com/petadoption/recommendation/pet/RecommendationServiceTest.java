package com.petadoption.recommendation.pet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationServiceTest {
  @Test
  void recommendPetsUsesCurrentPetAndAvailableCandidates() {
    PetSummary current = pet("current", "DOG", "Shanghai");
    PetSummary sameCity = pet("same-city", "CAT", "Shanghai");
    PetSummary unrelated = pet("unrelated", "CAT", "Beijing");
    PetCatalogClient petCatalogClient = new StubPetCatalogClient(current, List.of(sameCity, unrelated));
    RecommendationService recommendationService = new RecommendationService(
        petCatalogClient,
        new RecommendationRuleService());

    List<RecommendedPetResponse> recommendations = recommendationService.recommendPets(current.id());

    assertThat(recommendations)
        .extracting(RecommendedPetResponse::id)
        .containsExactly(sameCity.id());
  }

  private static PetSummary pet(String seed, String type, String city) {
    return new PetSummary(
        UUID.nameUUIDFromBytes(seed.getBytes()),
        seed,
        type,
        "MALE",
        18,
        city,
        "HEALTHY",
        "AVAILABLE",
        "https://example.com/" + seed + ".jpg",
        seed + " description",
        LocalDateTime.parse("2026-07-19T12:00:00"),
        LocalDateTime.parse("2026-07-19T12:00:00"));
  }

  private record StubPetCatalogClient(PetSummary current, List<PetSummary> availablePets) implements PetCatalogClient {
    @Override
    public PetSummary getPet(UUID id) {
      return current;
    }
  }
}
