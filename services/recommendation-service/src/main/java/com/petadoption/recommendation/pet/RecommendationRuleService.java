package com.petadoption.recommendation.pet;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
class RecommendationRuleService {
  List<RecommendedPetResponse> recommend(PetSummary current, List<PetSummary> candidates) {
    return candidates.stream()
        .filter(candidate -> !candidate.id().equals(current.id()))
        .filter(candidate -> candidate.type().equals(current.type()) || candidate.city().equals(current.city()))
        .limit(6)
        .map(PetSummary::toResponse)
        .toList();
  }
}
