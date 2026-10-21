package com.ecommerce.backend.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitMQConfig {

    public static final String TOPIC_EXCHANGE = "ecommerce.exchange";
    public static final String ORDER_QUEUE = "order.notifications";
    public static final String ORDER_ROUTING_KEY = "order.created";

    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderNotificationsQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderNotificationsQueue, TopicExchange ecommerceExchange) {
        return BindingBuilder.bind(orderNotificationsQueue).to(ecommerceExchange).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
