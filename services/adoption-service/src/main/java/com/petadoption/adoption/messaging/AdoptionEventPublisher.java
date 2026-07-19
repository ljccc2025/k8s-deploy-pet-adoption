package com.petadoption.adoption.messaging;

public interface AdoptionEventPublisher {
  void publish(AdoptionEvent event);
}
