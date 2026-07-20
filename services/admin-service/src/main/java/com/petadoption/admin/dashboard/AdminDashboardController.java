package com.petadoption.admin.dashboard;

import com.petadoption.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminDashboardController {
  private final DashboardService dashboardService;

  AdminDashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/api/v1/admin/dashboard")
  ApiResponse<DashboardSummary> summary() {
    return ApiResponse.success(dashboardService.summary());
  }
}
