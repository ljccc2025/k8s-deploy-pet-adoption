package com.petadoption.user.profile;

import com.petadoption.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ProfileExceptionHandler {
  @ExceptionHandler(UserProfileNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> notFound() {
    return ApiResponse.failure("user profile not found");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> badRequest(IllegalArgumentException exception) {
    return ApiResponse.failure(exception.getMessage());
  }
}
