package com.petadoption.pet.catalog;

import com.petadoption.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class PetExceptionHandler {
  @ExceptionHandler(PetNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> notFound() {
    return ApiResponse.failure("pet not found");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> validationFailed(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .orElse("invalid pet request");
    return ApiResponse.failure(message);
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> badRequest(Exception exception) {
    return ApiResponse.failure(exception.getMessage());
  }
}
