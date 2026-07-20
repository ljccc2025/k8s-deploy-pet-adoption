package com.petadoption.admin.dashboard;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean DashboardService dashboardService;

  @Test
  void dashboardReturnsSummaryWrappedInSuccessResponse() throws Exception {
    when(dashboardService.summary()).thenReturn(new DashboardSummary(12, 7, 3, 5));

    mockMvc.perform(get("/api/v1/admin/dashboard"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalPets").value(12))
        .andExpect(jsonPath("$.data.availablePets").value(7))
        .andExpect(jsonPath("$.data.pendingApplications").value(3))
        .andExpect(jsonPath("$.data.totalUsers").value(5));
  }
}
