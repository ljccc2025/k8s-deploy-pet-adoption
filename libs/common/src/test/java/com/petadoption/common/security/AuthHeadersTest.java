package com.petadoption.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthHeadersTest {
  @Test
  void exposesUserIdentityHeaders() {
    assertThat(AuthHeaders.USER_ID).isEqualTo("X-User-Id");
    assertThat(AuthHeaders.USER_ROLE).isEqualTo("X-User-Role");
  }
}
