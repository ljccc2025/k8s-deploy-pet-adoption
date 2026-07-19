package com.petadoption.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
  @Test
  void successWrapsData() {
    ApiResponse<String> response = ApiResponse.success("ok");

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isEqualTo("ok");
    assertThat(response.message()).isEqualTo("success");
  }

  @Test
  void errorResponseContainsCodeAndMessage() {
    ErrorResponse response = ErrorResponse.of("CODE", "message");

    assertThat(response.code()).isEqualTo("CODE");
    assertThat(response.message()).isEqualTo("message");
  }
}
