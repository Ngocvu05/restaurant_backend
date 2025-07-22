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

    // ✅ Exchange
    public static final String CHAT_EXCHANGE = "chat.exchange";

    // ✅ Queues
    public static final String CHAT_QUEUE = "chat.queue.user"; // dành cho user
    public static final String AI_QUEUE = "chat.queue.ai";     // queue nhận cho AI xử lý
    public static final String GUEST_CHAT_QUEUE = "chat.queue.guest"; // dành riêng cho guest chat
    public static final String RESPONSE_QUEUE = "chat.queue.response"; // phản hồi từ AI về
    public static final String SESSION_CONVERT_QUEUE = "chat.queue.session.convert"; // sau login

    // ✅ Routing keys
    public static final String CHAT_ROUTING_KEY = "chat.routing.user";
    public static final String GUEST_CHAT_ROUTING_KEY = "chat.routing.guest";
    public static final String AI_ROUTING_KEY = "chat.routing.ai";
    public static final String RESPONSE_ROUTING_KEY = "chat.response";
    public static final String CONVERT_SESSION_ROUTING_KEY = "chat.convert.session";

    // Exchange
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public Queue guestChatQueue() {
        return new Queue(GUEST_CHAT_QUEUE, true);
    }

    @Bean
    public Queue aiQueue() {
        return new Queue(AI_QUEUE, true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue sessionConversionQueue() {
        return new Queue(SESSION_CONVERT_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding userChatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with(CHAT_ROUTING_KEY);
    }

    @Bean
    public Binding guestChatBinding() {
        return BindingBuilder.bind(guestChatQueue())
                .to(chatExchange())
                .with(GUEST_CHAT_ROUTING_KEY);
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
                .with(RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding sessionConversionBinding() {
        return BindingBuilder.bind(sessionConversionQueue())
                .to(chatExchange())
                .with(CONVERT_SESSION_ROUTING_KEY);
    }

    // Message converter
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // RabbitTemplate sử dụng converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // RabbitListener container factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}
