package com.petadoption.adoption.application;

import com.petadoption.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
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

  @ExceptionHandler(PetCatalogNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> petNotFound(PetCatalogNotFoundException exception) {
    return ApiResponse.failure(exception.getMessage());
  }

  @ExceptionHandler(InvalidAdoptionStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiResponse<Void> invalidState(InvalidAdoptionStateException exception) {
    return ApiResponse.failure(exception.getMessage());
  }

  @ExceptionHandler({
      OptimisticLockingFailureException.class,
      ObjectOptimisticLockingFailureException.class
  })
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiResponse<Void> optimisticLockingFailed(Exception exception) {
    return ApiResponse.failure("adoption application was updated by another request");
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
