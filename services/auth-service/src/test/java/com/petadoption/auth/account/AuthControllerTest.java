package com.petadoption.auth.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.petadoption.auth.security.AuthenticatedToken;
import com.petadoption.auth.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
  @Autowired MockMvc mockMvc;
  @Autowired JwtTokenService jwtTokenService;

  @Test
  void registerAndLoginReturnsToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"user@example.com","password":"Password123","role":"USER"}
        """))
      .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"user@example.com","password":"Password123"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
  }

  @Test
  void registerRejectsAdminRole() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"admin-request@example.com","password":"Password123","role":"ADMIN"}
        """))
      .andExpect(status().isBadRequest());
  }

  @Test
  void loginTokenContainsUserIdentityAndRole() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"token-user@example.com","password":"Password123","role":"USER"}
        """))
      .andExpect(status().isOk());

    String loginBody = mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"token-user@example.com","password":"Password123"}
        """))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    String accessToken = JsonPath.read(loginBody, "$.data.accessToken");
    AuthenticatedToken authenticatedToken = jwtTokenService.parseToken(accessToken);

    assertThat(authenticatedToken.userId()).isNotNull();
    assertThat(authenticatedToken.email()).isEqualTo("token-user@example.com");
    assertThat(authenticatedToken.role()).isEqualTo("USER");
  }
}
