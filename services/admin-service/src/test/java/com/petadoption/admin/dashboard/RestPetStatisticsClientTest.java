package com.petadoption.admin.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestPetStatisticsClientTest {
  private MockRestServiceServer server;
  private RestPetStatisticsClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new RestPetStatisticsClient(builder, "http://pet-service");
  }

  @Test
  void totalPetsCountsAllReturnedPets() {
    server.expect(requestTo("http://pet-service/api/v1/pets"))
        .andRespond(withSuccess("""
            {
              "success": true,
              "message": "success",
              "data": [
                {"adoptionStatus": "AVAILABLE"},
                {"adoptionStatus": "ADOPTED"},
                {"adoptionStatus": "PENDING"}
              ]
            }
            """, MediaType.APPLICATION_JSON));

    long count = client.totalPets();

    assertThat(count).isEqualTo(3);
    server.verify();
  }

  @Test
  void availablePetsUsesAvailableStatusFilterAndCountsAvailableOnly() {
    server.expect(requestTo("http://pet-service/api/v1/pets?status=AVAILABLE"))
        .andRespond(withSuccess("""
            {
              "success": true,
              "message": "success",
              "data": [
                {"adoptionStatus": "AVAILABLE"},
                {"adoptionStatus": "ADOPTED"},
                {"adoptionStatus": "AVAILABLE"}
              ]
            }
            """, MediaType.APPLICATION_JSON));

    long count = client.availablePets();

    assertThat(count).isEqualTo(2);
    server.verify();
  }
}
