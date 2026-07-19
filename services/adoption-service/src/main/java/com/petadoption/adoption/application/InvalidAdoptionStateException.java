package com.petadoption.adoption.application;

class InvalidAdoptionStateException extends RuntimeException {
  InvalidAdoptionStateException(String message) {
    super(message);
  }
}
