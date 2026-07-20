package com.petadoption.recommendation.pet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestPetCatalogClientTest {
  private MockRestServiceServer server;
  private RestPetCatalogClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new RestPetCatalogClient(builder, "http://pet-service");
  }

  @Test
  void getPetFetchesPetDetailsById() {
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    server.expect(requestTo("http://pet-service/api/v1/pets/" + petId))
        .andRespond(withSuccess("""
            {
              "success": true,
              "message": "success",
              "data": {
                "id": "11111111-1111-1111-1111-111111111111",
                "name": "Mochi",
                "type": "CAT",
                "gender": "FEMALE",
                "ageMonths": 8,
                "city": "Shanghai",
                "healthStatus": "HEALTHY",
                "adoptionStatus": "AVAILABLE",
                "imageUrl": "https://example.com/mochi.jpg",
                "description": "Friendly kitten",
                "createdAt": "2026-07-19T12:00:00",
                "updatedAt": "2026-07-19T12:00:00"
              }
            }
            """, MediaType.APPLICATION_JSON));

    PetSummary pet = client.getPet(petId);

    assertThat(pet.name()).isEqualTo("Mochi");
    assertThat(pet.type()).isEqualTo("CAT");
    server.verify();
  }

  @Test
  void availablePetsFetchesAvailablePetsAndHandlesNullDataAsEmptyList() {
    server.expect(requestTo("http://pet-service/api/v1/pets?status=AVAILABLE"))
        .andRespond(withSuccess("""
            {
              "success": true,
              "message": "success",
              "data": null
            }
            """, MediaType.APPLICATION_JSON));

    List<PetSummary> pets = client.availablePets();

    assertThat(pets).isEmpty();
    server.verify();
  }
}
