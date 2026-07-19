package com.petadoption.adoption.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.petadoption.adoption.client.PetCatalogClient;
import com.petadoption.adoption.messaging.AdoptionEventPublisher;
import com.petadoption.common.security.AuthHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:adoption_service;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.schemas=adoption_schema",
    "spring.flyway.default-schema=adoption_schema",
    "spring.jpa.open-in-view=false",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.properties.hibernate.default_schema=adoption_schema"
})
@AutoConfigureMockMvc
class AdoptionApplicationControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean PetCatalogClient petCatalogClient;
  @MockitoBean AdoptionEventPublisher eventPublisher;

  @Test
  void submitApplication() throws Exception {
    mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, "11111111-1111-1111-1111-111111111111")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"petId":"22222222-2222-2222-2222-222222222222","reason":"我有稳定时间照顾它","experience":"曾经养过猫"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
  }
}
