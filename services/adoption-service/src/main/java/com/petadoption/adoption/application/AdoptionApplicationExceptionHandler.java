package com.petadoption.adoption.application;

import com.petadoption.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class AdoptionApplicationExceptionHandler {
  @ExceptionHandler(AdoptionApplicationNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> notFound() {
    return ApiResponse.failure("adoption application not found");
  }

  @ExceptionHandler(InvalidAdoptionStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiResponse<Void> invalidState(InvalidAdoptionStateException exception) {
    return ApiResponse.failure(exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> validationFailed(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .orElse("invalid adoption application request");
    return ApiResponse.failure(message);
  }

  @ExceptionHandler({
      ConstraintViolationException.class,
      IllegalArgumentException.class,
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class,
      MissingRequestHeaderException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> badRequest(Exception exception) {
    return ApiResponse.failure(exception.getMessage());
  }
}
