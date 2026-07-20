package com.petadoption.admin.dashboard;

public record DashboardSummary(
    long totalPets,
    long availablePets,
    long pendingApplications,
    long totalUsers) {
}
