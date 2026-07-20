package com.petadoption.admin.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DashboardServiceTest {
  @Test
  void downstreamFailureOnlyDegradesThatMetricToZero() {
    DashboardService service = new DashboardService(
        new FakePetStatisticsClient(9, new RuntimeException("available pets unavailable")),
        () -> 4,
        () -> 2);

    DashboardSummary summary = service.summary();

    assertThat(summary.totalPets()).isEqualTo(9);
    assertThat(summary.availablePets()).isZero();
    assertThat(summary.pendingApplications()).isEqualTo(4);
    assertThat(summary.totalUsers()).isEqualTo(2);
  }

  private record FakePetStatisticsClient(long totalPets, RuntimeException availablePetsFailure)
      implements PetStatisticsClient {
    @Override
    public long availablePets() {
      throw availablePetsFailure;
    }
  }
}
