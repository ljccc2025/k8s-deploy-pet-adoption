package com.petadoption.user.profile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.petadoption.common.security.AuthHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileControllerTest {
  private static final String USER_ID = "11111111-1111-1111-1111-111111111111";

  @Autowired MockMvc mockMvc;

  @Test
  void upsertAndGetCurrentUserProfile() throws Exception {
    mockMvc.perform(put("/api/v1/users/me/profile")
        .header(AuthHeaders.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"displayName":"Alice","phone":"13800000000","city":"上海","housing":"自有住房"}
        """))
      .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/users/me/profile")
        .header(AuthHeaders.USER_ID, USER_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.displayName").value("Alice"));
  }

  @Test
  void upsertRejectsTooLongDisplayName() throws Exception {
    String tooLongDisplayName = "A".repeat(101);

    mockMvc.perform(put("/api/v1/users/me/profile")
        .header(AuthHeaders.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"displayName":"%s","phone":"13800000000","city":"Shanghai","housing":"Owned"}
        """.formatted(tooLongDisplayName)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void upsertRejectsBlankDisplayName() throws Exception {
    mockMvc.perform(put("/api/v1/users/me/profile")
        .header(AuthHeaders.USER_ID, USER_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"displayName":" ","phone":"13800000000","city":"Shanghai","housing":"Owned"}
        """))
      .andExpect(status().isBadRequest());
  }
}
