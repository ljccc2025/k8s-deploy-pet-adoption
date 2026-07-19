package com.petadoption.adoption.messaging;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitMessageConfig {
  @Bean
  MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
