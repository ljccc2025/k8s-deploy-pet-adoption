package com.petadoption.auth.account;

import com.petadoption.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
  private final AuthService authService;

  AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  ApiResponse<Void> register(@RequestBody RegisterRequest request) {
    authService.register(request);
    return ApiResponse.success(null);
  }

  @PostMapping("/login")
  ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request));
  }
}
