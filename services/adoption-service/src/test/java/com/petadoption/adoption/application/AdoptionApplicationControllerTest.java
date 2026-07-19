package com.petadoption.adoption.application;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.petadoption.adoption.client.PetCatalogClient;
import com.petadoption.adoption.messaging.AdoptionEvent;
import com.petadoption.adoption.messaging.AdoptionEventPublisher;
import com.petadoption.common.events.AdoptionEvents;
import com.petadoption.common.security.AuthHeaders;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
@Sql(statements = "DELETE FROM adoption_schema.adoption_applications")
class AdoptionApplicationControllerTest {
  private static final String USER_ID = "11111111-1111-1111-1111-111111111111";
  private static final String OTHER_USER_ID = "33333333-3333-3333-3333-333333333333";
  private static final String REVIEWER_ID = "44444444-4444-4444-4444-444444444444";
  private static final String PET_ID = "22222222-2222-2222-2222-222222222222";

  @Autowired MockMvc mockMvc;

  @MockitoBean PetCatalogClient petCatalogClient;
  @MockitoBean AdoptionEventPublisher eventPublisher;

  @BeforeEach
  void resetMocks() {
    reset(petCatalogClient, eventPublisher);
  }

  @Test
  void submitApplication() throws Exception {
    mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(applicationJson(PET_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

    verify(petCatalogClient).requireAvailable(UUID.fromString(PET_ID));
  }

  @Test
  void rejectUnavailablePetWithoutSavingOrExternalEffects() throws Exception {
    doThrow(new InvalidAdoptionStateException("pet is not available"))
        .when(petCatalogClient).requireAvailable(UUID.fromString(PET_ID));

    mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(applicationJson(PET_ID)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(get("/api/v1/admin/adoptions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
    verify(petCatalogClient).requireAvailable(UUID.fromString(PET_ID));
    verify(petCatalogClient, never()).updateAdoptionStatus(any(), any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void submitSuccessSchedulesPetPendingAndSubmittedEvent() throws Exception {
    submitAndReturnId(USER_ID, PET_ID);

    InOrder inOrder = inOrder(petCatalogClient, eventPublisher);
    inOrder.verify(petCatalogClient).requireAvailable(UUID.fromString(PET_ID));
    inOrder.verify(petCatalogClient).updateAdoptionStatus(UUID.fromString(PET_ID), "PENDING");
    AdoptionEvent event = captureOnlyEvent();
    org.assertj.core.api.Assertions.assertThat(event.eventType()).isEqualTo(AdoptionEvents.SUBMITTED);
    org.assertj.core.api.Assertions.assertThat(event.petId()).isEqualTo(UUID.fromString(PET_ID));
    org.assertj.core.api.Assertions.assertThat(event.userId()).isEqualTo(UUID.fromString(USER_ID));
    org.assertj.core.api.Assertions.assertThat(event.applicationId()).isNotNull();
    org.assertj.core.api.Assertions.assertThat(event.occurredAt()).isNotNull();
  }

  @Test
  void listMineOnlyReturnsCurrentUserApplications() throws Exception {
    String minePetId = "55555555-5555-5555-5555-555555555555";
    String otherPetId = "66666666-6666-6666-6666-666666666666";
    submitAndReturnId(USER_ID, minePetId);
    submitAndReturnId(OTHER_USER_ID, otherPetId);

    mockMvc.perform(get("/api/v1/adoptions/me")
            .header(AuthHeaders.USER_ID, USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].userId").value(USER_ID))
        .andExpect(jsonPath("$.data[0].petId").value(minePetId));
  }

  @Test
  void listAllReturnsAdminApplicationList() throws Exception {
    String firstPetId = "77777777-7777-7777-7777-777777777777";
    String secondPetId = "88888888-8888-8888-8888-888888888888";
    submitAndReturnId(USER_ID, firstPetId);
    submitAndReturnId(OTHER_USER_ID, secondPetId);

    mockMvc.perform(get("/api/v1/admin/adoptions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[*].petId").value(containsInAnyOrder(firstPetId, secondPetId)));
  }

  @Test
  void approveApplicationSchedulesPetAdoptedAndApprovedEvent() throws Exception {
    String applicationId = submitAndReturnId(USER_ID, PET_ID);
    reset(petCatalogClient, eventPublisher);

    mockMvc.perform(post("/api/v1/admin/adoptions/{id}/approve", applicationId)
            .header(AuthHeaders.USER_ID, REVIEWER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("APPROVED"))
        .andExpect(jsonPath("$.data.reviewerId").value(REVIEWER_ID));

    verify(petCatalogClient).updateAdoptionStatus(UUID.fromString(PET_ID), "ADOPTED");
    AdoptionEvent event = captureOnlyEvent();
    org.assertj.core.api.Assertions.assertThat(event.eventType()).isEqualTo(AdoptionEvents.APPROVED);
    org.assertj.core.api.Assertions.assertThat(event.applicationId()).isEqualTo(UUID.fromString(applicationId));
  }

  @Test
  void rejectApplicationSavesCommentAndPublishesRejectedEvent() throws Exception {
    String applicationId = submitAndReturnId(USER_ID, PET_ID);
    reset(petCatalogClient, eventPublisher);

    mockMvc.perform(post("/api/v1/admin/adoptions/{id}/reject", applicationId)
            .header(AuthHeaders.USER_ID, REVIEWER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"reviewComment":"not a good match"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("REJECTED"))
        .andExpect(jsonPath("$.data.reviewComment").value("not a good match"));

    verify(petCatalogClient, never()).updateAdoptionStatus(any(), any());
    AdoptionEvent event = captureOnlyEvent();
    org.assertj.core.api.Assertions.assertThat(event.eventType()).isEqualTo(AdoptionEvents.REJECTED);
    org.assertj.core.api.Assertions.assertThat(event.applicationId()).isEqualTo(UUID.fromString(applicationId));
  }

  @Test
  void applicantCanCancelAndNonApplicantGetsNotFound() throws Exception {
    String cancellableId = submitAndReturnId(USER_ID, PET_ID);
    String protectedId = submitAndReturnId(USER_ID, "99999999-9999-9999-9999-999999999991");
    reset(petCatalogClient, eventPublisher);

    mockMvc.perform(post("/api/v1/adoptions/{id}/cancel", protectedId)
            .header(AuthHeaders.USER_ID, OTHER_USER_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(post("/api/v1/adoptions/{id}/cancel", cancellableId)
            .header(AuthHeaders.USER_ID, USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));

    AdoptionEvent event = captureOnlyEvent();
    org.assertj.core.api.Assertions.assertThat(event.eventType()).isEqualTo(AdoptionEvents.CANCELLED);
    org.assertj.core.api.Assertions.assertThat(event.applicationId()).isEqualTo(UUID.fromString(cancellableId));
  }

  @Test
  void terminalApplicationCannotTransitionAgain() throws Exception {
    String approvedId = submitAndReturnId(USER_ID, PET_ID);
    mockMvc.perform(post("/api/v1/admin/adoptions/{id}/approve", approvedId)
            .header(AuthHeaders.USER_ID, REVIEWER_ID))
        .andExpect(status().isOk());
    reset(petCatalogClient, eventPublisher);

    mockMvc.perform(post("/api/v1/admin/adoptions/{id}/approve", approvedId)
            .header(AuthHeaders.USER_ID, REVIEWER_ID))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
    mockMvc.perform(post("/api/v1/admin/adoptions/{id}/reject", approvedId)
            .header(AuthHeaders.USER_ID, REVIEWER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"reviewComment":"late reject"}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
    mockMvc.perform(post("/api/v1/adoptions/{id}/cancel", approvedId)
            .header(AuthHeaders.USER_ID, USER_ID))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));

    verifyNoInteractions(petCatalogClient, eventPublisher);
  }

  @Test
  void blankReasonOrExperienceReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"petId":"%s","reason":"","experience":" "}
                """.formatted(PET_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verifyNoInteractions(petCatalogClient, eventPublisher);
  }

  @Test
  void missingOrInvalidUserHeaderReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/adoptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(applicationJson(PET_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, "not-a-uuid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(applicationJson(PET_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verifyNoInteractions(petCatalogClient, eventPublisher);
  }

  private String submitAndReturnId(String userId, String petId) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/adoptions")
            .header(AuthHeaders.USER_ID, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(applicationJson(petId)))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }

  private AdoptionEvent captureOnlyEvent() {
    ArgumentCaptor<AdoptionEvent> eventCaptor = ArgumentCaptor.forClass(AdoptionEvent.class);
    verify(eventPublisher).publish(eventCaptor.capture());
    return eventCaptor.getValue();
  }

  private static String applicationJson(String petId) {
    return """
        {"petId":"%s","reason":"I have time to care for this pet","experience":"I have adopted pets before"}
        """.formatted(petId);
  }
}
