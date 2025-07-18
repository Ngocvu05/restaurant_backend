package com.management.chat_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AI_ROUTING_KEY = "chat.ai";
    public static final String AI_QUEUE = "chat.ai.queue";

    public static final String CHAT_QUEUE = "chat-messages";
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_ROUTING_KEY = "chat.message";
    public static final String CHAT_RESPONSE_ROUTING_KEY = "chat.response";

    public static final String CONVERT_SESSION_ROUTING_KEY = "chat.convert.session";

    // Exchange
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    // Chat Queue
    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public Queue sessionConversionQueue() {
        return new Queue(CONVERT_SESSION_ROUTING_KEY, true); // durable = true
    }


    // Chat Binding
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(CHAT_ROUTING_KEY);
    }

    // AI Queue
    @Bean
    public Queue aiQueue() {
        return new Queue(AI_QUEUE, true);
    }

    // AI Binding
    @Bean
    public Binding aiBinding() {
        return BindingBuilder.bind(aiQueue()).to(chatExchange()).with(AI_ROUTING_KEY);
    }

    // JSON converter
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean(name = "customRabbitTemplate")
    public RabbitTemplate customRabbitTemplate(ConnectionFactory connectionFactory,
                                               MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(CHAT_RESPONSE_ROUTING_KEY, true);
    }

    @Bean
    public Binding responseBinding() {
        return BindingBuilder.bind(responseQueue())
                .to(chatExchange())
                .with(CHAT_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding bindingSessionConversionQueue() {
        return BindingBuilder
                .bind(sessionConversionQueue())
                .to(chatExchange())
                .with(CONVERT_SESSION_ROUTING_KEY);
    }
}
