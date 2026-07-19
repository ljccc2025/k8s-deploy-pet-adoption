package com.petadoption.notification.notice;

import com.petadoption.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class NotificationExceptionHandler {
  @ExceptionHandler(NotificationNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> notFound() {
    return ApiResponse.failure("notification not found");
  }

  @ExceptionHandler({
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
