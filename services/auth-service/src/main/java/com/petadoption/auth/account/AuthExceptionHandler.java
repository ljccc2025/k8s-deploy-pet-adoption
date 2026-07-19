package com.petadoption.auth.account;

import com.petadoption.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AuthExceptionHandler {
  @ExceptionHandler(DuplicateAccountException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiResponse<Void> duplicateAccount() {
    return ApiResponse.failure("account already exists");
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  ApiResponse<Void> invalidCredentials() {
    return ApiResponse.failure("invalid credentials");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> badRequest(IllegalArgumentException exception) {
    return ApiResponse.failure(exception.getMessage());
  }
}
