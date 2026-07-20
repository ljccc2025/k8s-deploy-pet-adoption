package com.petadoption.recommendation.pet;

import com.petadoption.common.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class RestPetCatalogClient implements PetCatalogClient {
  private final RestClient restClient;

  RestPetCatalogClient(RestClient.Builder builder, @Value("${pet-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @Override
  public PetSummary getPet(UUID id) {
    ApiResponse<PetSummary> response = restClient.get()
        .uri("/api/v1/pets/{id}", id)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });
    return response == null ? null : response.data();
  }

  @Override
  public List<PetSummary> availablePets() {
    ApiResponse<List<PetSummary>> response = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/api/v1/pets").queryParam("status", "AVAILABLE").build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });
    return response == null || response.data() == null ? List.of() : response.data();
  }
}
