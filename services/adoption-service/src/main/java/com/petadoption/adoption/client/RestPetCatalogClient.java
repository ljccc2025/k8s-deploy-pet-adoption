package com.petadoption.adoption.client;

import com.petadoption.adoption.application.InvalidAdoptionStateException;
import com.petadoption.adoption.application.PetCatalogNotFoundException;
import com.petadoption.common.api.ApiResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
class RestPetCatalogClient implements PetCatalogClient {
  private static final String AVAILABLE = "AVAILABLE";

  private final RestClient restClient;

  RestPetCatalogClient(RestClient.Builder builder, @Value("${pet-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @Override
  public void requireAvailable(UUID petId) {
    ApiResponse<PetCatalogResponse> response = getPet(petId);
    String adoptionStatus = response == null || response.data() == null
        ? null
        : response.data().adoptionStatus();
    if (!AVAILABLE.equals(adoptionStatus)) {
      throw new InvalidAdoptionStateException("pet is not available for adoption");
    }
  }

  private ApiResponse<PetCatalogResponse> getPet(UUID petId) {
    try {
      return restClient.get()
          .uri("/api/v1/pets/{id}", petId)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });
    } catch (RestClientResponseException exception) {
      if (exception.getStatusCode().value() == 404) {
        throw new PetCatalogNotFoundException(petId);
      }
      throw new InvalidAdoptionStateException("pet catalog request failed: " + exception.getStatusCode().value());
    }
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

  private record PetCatalogResponse(String adoptionStatus) {
  }
}
