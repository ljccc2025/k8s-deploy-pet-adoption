package com.petadoption.adoption.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class RabbitAdoptionEventPublisher implements AdoptionEventPublisher {
  private final RabbitTemplate rabbitTemplate;
  private final String exchange;
  private final String routingKey;

  RabbitAdoptionEventPublisher(
      RabbitTemplate rabbitTemplate,
      @Value("${adoption.events.exchange}") String exchange,
      @Value("${adoption.events.routing-key}") String routingKey) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
    this.routingKey = routingKey;
  }

  @Override
  public void publish(AdoptionEvent event) {
    rabbitTemplate.convertAndSend(exchange, routingKey, event);
  }
}
