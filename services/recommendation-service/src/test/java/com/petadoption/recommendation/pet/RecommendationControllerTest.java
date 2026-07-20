package com.petadoption.recommendation.pet;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean RecommendationService recommendationService;

  @Test
  void recommendedPetsReturnsSuccessResponseForRequestedPet() throws Exception {
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID recommendedId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    when(recommendationService.recommendPets(petId)).thenReturn(List.of(new RecommendedPetResponse(
        recommendedId,
        "Mochi",
        "CAT",
        "FEMALE",
        8,
        "Shanghai",
        "HEALTHY",
        "AVAILABLE",
        "https://example.com/mochi.jpg",
        "Friendly kitten",
        LocalDateTime.parse("2026-07-19T12:00:00"),
        LocalDateTime.parse("2026-07-19T12:00:00"))));

    mockMvc.perform(get("/api/v1/recommendations/pets").param("petId", petId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(recommendedId.toString()))
        .andExpect(jsonPath("$.data[0].name").value("Mochi"))
        .andExpect(jsonPath("$.data[0].type").value("CAT"))
        .andExpect(jsonPath("$.data[0].city").value("Shanghai"));
  }
}
