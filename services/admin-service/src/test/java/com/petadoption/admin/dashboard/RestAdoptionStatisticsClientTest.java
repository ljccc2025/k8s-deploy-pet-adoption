package com.petadoption.admin.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestAdoptionStatisticsClientTest {
  private MockRestServiceServer server;
  private RestAdoptionStatisticsClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new RestAdoptionStatisticsClient(builder, "http://adoption-service");
  }

  @Test
  void pendingApplicationsOnlyCountsSubmittedStatus() {
    server.expect(requestTo("http://adoption-service/api/v1/admin/adoptions"))
        .andRespond(withSuccess("""
            {
              "success": true,
              "message": "success",
              "data": [
                {"status": "SUBMITTED"},
                {"status": "APPROVED"},
                {"status": "SUBMITTED"},
                {"status": "REJECTED"}
              ]
            }
            """, MediaType.APPLICATION_JSON));

    long count = client.pendingApplications();

    assertThat(count).isEqualTo(2);
    server.verify();
  }
}
