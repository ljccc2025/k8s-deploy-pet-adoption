package com.petadoption.notification.events;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
class RabbitNotificationConfig {
  @Bean
  Queue adoptionNotificationsQueue(@Value("${notification.events.queue}") String queue) {
    return new Queue(queue, true);
  }

  @Bean
  TopicExchange adoptionEventsExchange(@Value("${notification.events.exchange}") String exchange) {
    return new TopicExchange(exchange, true, false);
  }

  @Bean
  Binding adoptionNotificationsBinding(
      Queue adoptionNotificationsQueue,
      TopicExchange adoptionEventsExchange,
      @Value("${notification.events.routing-key}") String routingKey) {
    return BindingBuilder.bind(adoptionNotificationsQueue).to(adoptionEventsExchange).with(routingKey);
  }

  @Bean
  MessageConverter jsonMessageConverter() {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
    typeMapper.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.INFERRED);
    converter.setJavaTypeMapper(typeMapper);
    return converter;
  }
}
