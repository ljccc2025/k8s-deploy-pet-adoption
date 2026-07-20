package com.petadoption.gateway.security;

import com.petadoption.common.security.AuthHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
class GatewayAuthFilter implements GlobalFilter, Ordered {
  private static final String BEARER_PREFIX = "Bearer ";

  private final RouteSecurity routeSecurity;
  private final GatewayJwtService jwtService;

  GatewayAuthFilter(RouteSecurity routeSecurity, GatewayJwtService jwtService) {
    this.routeSecurity = routeSecurity;
    this.jwtService = jwtService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerWebExchange sanitizedExchange = withoutTrustedUserHeaders(exchange);
    String path = sanitizedExchange.getRequest().getPath().pathWithinApplication().value();
    if (routeSecurity.isPublicPath(path)) {
      return chain.filter(sanitizedExchange);
    }

    String authorization = sanitizedExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
      return unauthorized(sanitizedExchange);
    }

    return jwtService.authenticate(authorization.substring(BEARER_PREFIX.length()))
        .map(user -> chain.filter(withUserHeaders(sanitizedExchange, user)))
        .orElseGet(() -> unauthorized(sanitizedExchange));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private static ServerWebExchange withUserHeaders(ServerWebExchange exchange, AuthenticatedGatewayUser user) {
    ServerHttpRequest request = exchange.getRequest()
        .mutate()
        .headers(headers -> {
          headers.set(AuthHeaders.USER_ID, user.userId().toString());
          headers.set(AuthHeaders.USER_ROLE, user.role());
        })
        .build();
    return exchange.mutate().request(request).build();
  }

  private static ServerWebExchange withoutTrustedUserHeaders(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest()
        .mutate()
        .headers(headers -> {
          headers.remove(AuthHeaders.USER_ID);
          headers.remove(AuthHeaders.USER_ROLE);
        })
        .build();
    return exchange.mutate().request(request).build();
  }

  private static Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
