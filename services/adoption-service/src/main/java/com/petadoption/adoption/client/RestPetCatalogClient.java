package com.petadoption.adoption.client;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class RestPetCatalogClient implements PetCatalogClient {
  private final RestClient restClient;

  RestPetCatalogClient(RestClient.Builder builder, @Value("${pet-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @Override
  public void updateAdoptionStatus(UUID petId, String adoptionStatus) {
    restClient.patch()
        .uri("/api/v1/internal/pets/{id}/adoption-status", petId)
        .body(new UpdateAdoptionStatusRequest(adoptionStatus))
        .retrieve()
        .toBodilessEntity();
  }

  private record UpdateAdoptionStatusRequest(String adoptionStatus) {
  }
}
