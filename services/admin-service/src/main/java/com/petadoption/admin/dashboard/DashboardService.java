package com.petadoption.admin.dashboard;

import java.util.function.LongSupplier;
import org.springframework.stereotype.Service;

@Service
class DashboardService {
  private final PetStatisticsClient petStatisticsClient;
  private final AdoptionStatisticsClient adoptionStatisticsClient;
  private final UserStatisticsClient userStatisticsClient;

  DashboardService(
      PetStatisticsClient petStatisticsClient,
      AdoptionStatisticsClient adoptionStatisticsClient,
      UserStatisticsClient userStatisticsClient) {
    this.petStatisticsClient = petStatisticsClient;
    this.adoptionStatisticsClient = adoptionStatisticsClient;
    this.userStatisticsClient = userStatisticsClient;
  }

  DashboardSummary summary() {
    return new DashboardSummary(
        metricOrZero(petStatisticsClient::totalPets),
        metricOrZero(petStatisticsClient::availablePets),
        metricOrZero(adoptionStatisticsClient::pendingApplications),
        metricOrZero(userStatisticsClient::totalUsers));
  }

  private static long metricOrZero(LongSupplier supplier) {
    try {
      return supplier.getAsLong();
    } catch (RuntimeException exception) {
      return 0;
    }
  }
}
