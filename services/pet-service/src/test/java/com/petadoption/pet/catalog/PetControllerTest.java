package com.petadoption.pet.catalog;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerTest {
  private static final String AVAILABLE_PET_ID = "11111111-2222-3333-4444-555555555555";
  private static final String MISSING_PET_ID = "99999999-9999-9999-9999-999999999999";

  @Autowired MockMvc mockMvc;

  @Test
  void listAvailablePets() throws Exception {
    mockMvc.perform(get("/api/v1/pets?status=AVAILABLE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data[0].adoptionStatus").value("AVAILABLE"));
  }

  @Test
  void getPetDetails() throws Exception {
    mockMvc.perform(get("/api/v1/pets/{id}", AVAILABLE_PET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(AVAILABLE_PET_ID))
        .andExpect(jsonPath("$.data.name").value("Milo"))
        .andExpect(jsonPath("$.data.adoptionStatus").value("AVAILABLE"));
  }

  @Test
  void createPet() throws Exception {
    mockMvc.perform(post("/api/v1/admin/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name":"Nora",
                  "type":"Cat",
                  "gender":"Female",
                  "ageMonths":18,
                  "city":"Shanghai",
                  "healthStatus":"Vaccinated",
                  "adoptionStatus":"AVAILABLE",
                  "imageUrl":"https://example.com/nora.jpg",
                  "description":"Calm indoor companion"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value("Nora"))
        .andExpect(jsonPath("$.data.adoptionStatus").value("AVAILABLE"));
  }

  @Test
  void updatePet() throws Exception {
    String petId = createPetAndReturnId("Buddy", "Dog");

    mockMvc.perform(put("/api/v1/admin/pets/{id}", petId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name":"Milo Updated",
                  "type":"Dog",
                  "gender":"Male",
                  "ageMonths":13,
                  "city":"Hangzhou",
                  "healthStatus":"Vaccinated and dewormed",
                  "adoptionStatus":"PENDING",
                  "imageUrl":"https://example.com/milo-updated.jpg",
                  "description":"Friendly young dog"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(petId))
        .andExpect(jsonPath("$.data.name").value("Milo Updated"))
        .andExpect(jsonPath("$.data.city").value("Hangzhou"))
        .andExpect(jsonPath("$.data.adoptionStatus").value("PENDING"));
  }

  @Test
  void updateAdoptionStatus() throws Exception {
    String petId = createPetAndReturnId("Poppy", "Cat");

    mockMvc.perform(patch("/api/v1/internal/pets/{id}/adoption-status", petId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"adoptionStatus":"ADOPTED"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(petId))
        .andExpect(jsonPath("$.data.adoptionStatus").value("ADOPTED"));
  }

  @Test
  void rejectInvalidStatus() throws Exception {
    mockMvc.perform(get("/api/v1/pets?status=UNKNOWN"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void returnNotFoundForMissingPet() throws Exception {
    mockMvc.perform(get("/api/v1/pets/{id}", MISSING_PET_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }

  private String createPetAndReturnId(String name, String type) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/admin/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name":"%s",
                  "type":"%s",
                  "gender":"Female",
                  "ageMonths":10,
                  "city":"Shanghai",
                  "healthStatus":"Vaccinated",
                  "adoptionStatus":"AVAILABLE",
                  "imageUrl":null,
                  "description":"Created for controller test isolation"
                }
                """.formatted(name, type)))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }
}
