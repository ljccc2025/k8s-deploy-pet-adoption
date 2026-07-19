package com.petadoption.adoption.application;

public class InvalidAdoptionStateException extends RuntimeException {
  public InvalidAdoptionStateException(String message) {
    super(message);
  }
}
