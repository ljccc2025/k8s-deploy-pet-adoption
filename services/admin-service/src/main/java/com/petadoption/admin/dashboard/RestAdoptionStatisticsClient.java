package com.petadoption.admin.dashboard;

import com.petadoption.common.api.ApiResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class RestAdoptionStatisticsClient implements AdoptionStatisticsClient {
  private final RestClient restClient;

  RestAdoptionStatisticsClient(RestClient.Builder builder, @Value("${adoption-service.base-url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  @Override
  public long pendingApplications() {
    ApiResponse<List<AdoptionApplicationResponse>> response = restClient.get()
        .uri("/api/v1/admin/adoptions")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });
    List<AdoptionApplicationResponse> applications = response == null || response.data() == null
        ? List.of()
        : response.data();
    return applications.stream()
        .filter(application -> "SUBMITTED".equals(application.status()))
        .count();
  }

  private record AdoptionApplicationResponse(String status) {
  }
}
