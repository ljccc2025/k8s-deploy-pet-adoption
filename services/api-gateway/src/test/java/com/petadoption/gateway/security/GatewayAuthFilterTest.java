package com.petadoption.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.petadoption.common.security.AuthHeaders;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class GatewayAuthFilterTest {

  private final RouteSecurity routeSecurity = new RouteSecurity();
  private final GatewayJwtService jwtService = mock(GatewayJwtService.class);
  private final GatewayAuthFilter filter = new GatewayAuthFilter(routeSecurity, jwtService);

  @Test
  void publicPathsAreAllowedWithoutJwt() {
    assertThat(routeSecurity.isPublicPath("/api/v1/auth/login")).isTrue();
    assertThat(routeSecurity.isPublicPath("/api/v1/pets")).isTrue();
    assertThat(routeSecurity.isPublicPath("/api/v1/pets/11111111-1111-1111-1111-111111111111"))
        .isTrue();
    assertThat(routeSecurity.isPublicPath("/api/v1/adoptions")).isFalse();
  }

  @Test
  void publicPathsDropSpoofedTrustedHeaders() {
    MockServerWebExchange exchange = MockServerWebExchange.from(
        MockServerHttpRequest.get("/api/v1/pets")
            .header(AuthHeaders.USER_ID, "22222222-2222-2222-2222-222222222222")
            .header(AuthHeaders.USER_ROLE, "ADMIN")
            .build());
    CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();

    filter.filter(exchange, chain).block();

    ServerWebExchange downstreamExchange = chain.exchange();
    assertThat(chain.wasCalled()).isTrue();
    assertThat(downstreamExchange.getRequest().getHeaders()).doesNotContainKey(AuthHeaders.USER_ID);
    assertThat(downstreamExchange.getRequest().getHeaders()).doesNotContainKey(AuthHeaders.USER_ROLE);
    verify(jwtService, never()).authenticate(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void protectedPathWithoutBearerTokenReturnsUnauthorized() {
    MockServerWebExchange exchange = exchange("/api/v1/adoptions");
    CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();

    filter.filter(exchange, chain).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(chain.wasCalled()).isFalse();
    verify(jwtService, never()).authenticate(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void protectedPathWithInvalidBearerTokenReturnsUnauthorized() {
    MockServerWebExchange exchange = exchangeWithBearer("/api/v1/adoptions", "invalid-token");
    CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();
    when(jwtService.authenticate("invalid-token")).thenReturn(Optional.empty());

    filter.filter(exchange, chain).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(chain.wasCalled()).isFalse();
  }

  @Test
  void protectedPathWithValidBearerTokenAddsUserHeadersAndCallsChain() {
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    MockServerWebExchange exchange = exchangeWithBearer("/api/v1/adoptions", "valid-token");
    CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();
    when(jwtService.authenticate("valid-token"))
        .thenReturn(Optional.of(new AuthenticatedGatewayUser(userId, "ADMIN")));

    filter.filter(exchange, chain).block();

    ServerWebExchange downstreamExchange = chain.exchange();
    assertThat(chain.wasCalled()).isTrue();
    assertThat(downstreamExchange.getRequest().getHeaders().getFirst(AuthHeaders.USER_ID))
        .isEqualTo(userId.toString());
    assertThat(downstreamExchange.getRequest().getHeaders().getFirst(AuthHeaders.USER_ROLE))
        .isEqualTo("ADMIN");
  }

  @Test
  void trustedUserHeadersReplaceSpoofedIncomingHeaders() {
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    MockServerWebExchange exchange = MockServerWebExchange.from(
        MockServerHttpRequest.get("/api/v1/adoptions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
            .header(AuthHeaders.USER_ID, "22222222-2222-2222-2222-222222222222")
            .header(AuthHeaders.USER_ROLE, "ADMIN")
            .build());
    CapturingGatewayFilterChain chain = new CapturingGatewayFilterChain();
    when(jwtService.authenticate("valid-token"))
        .thenReturn(Optional.of(new AuthenticatedGatewayUser(userId, "USER")));

    filter.filter(exchange, chain).block();

    ServerWebExchange downstreamExchange = chain.exchange();
    assertThat(downstreamExchange.getRequest().getHeaders().get(AuthHeaders.USER_ID))
        .containsExactly(userId.toString());
    assertThat(downstreamExchange.getRequest().getHeaders().get(AuthHeaders.USER_ROLE))
        .containsExactly("USER");
  }

  private static MockServerWebExchange exchange(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }

  private static MockServerWebExchange exchangeWithBearer(String path, String token) {
    return MockServerWebExchange.from(
        MockServerHttpRequest.get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build());
  }

  private static final class CapturingGatewayFilterChain implements GatewayFilterChain {
    private final AtomicBoolean called = new AtomicBoolean();
    private final AtomicReference<ServerWebExchange> exchange = new AtomicReference<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange) {
      this.called.set(true);
      this.exchange.set(exchange);
      return Mono.empty();
    }

    boolean wasCalled() {
      return called.get();
    }

    ServerWebExchange exchange() {
      return exchange.get();
    }
  }
}
