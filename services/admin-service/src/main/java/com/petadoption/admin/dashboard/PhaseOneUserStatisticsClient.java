package com.petadoption.admin.dashboard;

import org.springframework.stereotype.Service;

@Service
class PhaseOneUserStatisticsClient implements UserStatisticsClient {
  @Override
  public long totalUsers() {
    // Phase 1: user-service has no admin count endpoint, and admin-service must not read its database.
    return 0;
  }
}
