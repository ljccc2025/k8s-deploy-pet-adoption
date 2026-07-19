package com.petadoption.adoption.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.petadoption.adoption.application.InvalidAdoptionStateException;
import com.petadoption.adoption.application.PetCatalogNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestPetCatalogClientTest {
  private static final UUID PET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private MockRestServiceServer server;
  private RestPetCatalogClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new RestPetCatalogClient(builder, "http://pet-service");
  }

  @Test
  void requireAvailableAllowsAvailablePet() {
    server.expect(requestTo("http://pet-service/api/v1/pets/" + PET_ID))
        .andRespond(withSuccess("""
            {"success":true,"message":"success","data":{"adoptionStatus":"AVAILABLE"}}
            """, MediaType.APPLICATION_JSON));

    assertThatCode(() -> client.requireAvailable(PET_ID)).doesNotThrowAnyException();

    server.verify();
  }

  @Test
  void requireAvailableMapsNotFoundToPetCatalogNotFoundException() {
    server.expect(requestTo("http://pet-service/api/v1/pets/" + PET_ID))
        .andRespond(withStatus(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {"success":false,"message":"pet not found","data":null}
                """));

    assertThatThrownBy(() -> client.requireAvailable(PET_ID))
        .isInstanceOf(PetCatalogNotFoundException.class);

    server.verify();
  }

  @Test
  void requireAvailableMapsNonAvailablePetToInvalidAdoptionStateException() {
    server.expect(requestTo("http://pet-service/api/v1/pets/" + PET_ID))
        .andRespond(withSuccess("""
            {"success":true,"message":"success","data":{"adoptionStatus":"PENDING"}}
            """, MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> client.requireAvailable(PET_ID))
        .isInstanceOf(InvalidAdoptionStateException.class);

    server.verify();
  }
}
