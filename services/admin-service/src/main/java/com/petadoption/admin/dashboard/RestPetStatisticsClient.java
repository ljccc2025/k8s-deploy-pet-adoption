package com.petadoption.admin.dashboard;

import com.petadoption.common.api.ApiResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class RestPetStatisticsClient implements PetStatisticsClient {
  private final RestClient restClient;

  RestPetStatisticsClient(RestClient.Builder builder, @Value("${pet-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @Override
  public long totalPets() {
    return pets(null).size();
  }

  @Override
  public long availablePets() {
    return pets("AVAILABLE").stream()
        .filter(pet -> "AVAILABLE".equals(pet.adoptionStatus()))
        .count();
  }

  private List<PetResponse> pets(String status) {
    ApiResponse<List<PetResponse>> response = status == null
        ? restClient.get()
            .uri("/api/v1/pets")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            })
        : restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/api/v1/pets").queryParam("status", status).build())
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
    return response == null || response.data() == null ? List.of() : response.data();
  }

  private record PetResponse(String adoptionStatus) {
  }
}
