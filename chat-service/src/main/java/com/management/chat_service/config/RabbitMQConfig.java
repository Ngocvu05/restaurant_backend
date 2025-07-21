package com.management.chat_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Exchange names
    public static final String CHAT_EXCHANGE = "chat.exchange";

    // Queue names
    public static final String CHAT_QUEUE = "chat-messages";
    public static final String AI_QUEUE = "chat.ai.queue";

    // Routing keys
    public static final String CHAT_ROUTING_KEY = "chat.message";
    public static final String AI_ROUTING_KEY = "chat.ai";
    public static final String CHAT_RESPONSE_ROUTING_KEY = "chat.response";
    public static final String CONVERT_SESSION_ROUTING_KEY = "chat.convert.session";

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public Queue aiQueue() {
        return new Queue(AI_QUEUE, true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(CHAT_RESPONSE_ROUTING_KEY, true);
    }

    @Bean
    public Queue sessionConversionQueue() {
        return new Queue(CONVERT_SESSION_ROUTING_KEY, true);
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with(CHAT_ROUTING_KEY);
    }

    @Bean
    public Binding aiBinding() {
        return BindingBuilder.bind(aiQueue())
                .to(chatExchange())
                .with(AI_ROUTING_KEY);
    }

    @Bean
    public Binding responseBinding() {
        return BindingBuilder.bind(responseQueue())
                .to(chatExchange())
                .with(CHAT_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding sessionConversionBinding() {
        return BindingBuilder.bind(sessionConversionQueue())
                .to(chatExchange())
                .with(CONVERT_SESSION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // Config retry (optional)
        factory.setDefaultRequeueRejected(false);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}
