package com.petadoption.file;

import com.petadoption.common.api.ApiResponse;
import java.io.UncheckedIOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
class FileExceptionHandler {
  @ExceptionHandler(FileNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiResponse<Void> notFound() {
    return ApiResponse.failure("file not found");
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestPartException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiResponse<Void> badRequest(Exception exception) {
    return ApiResponse.failure(exception.getMessage());
  }

  @ExceptionHandler(UncheckedIOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ApiResponse<Void> storageFailed() {
    return ApiResponse.failure("failed to store file");
  }
}
