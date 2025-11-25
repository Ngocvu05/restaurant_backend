package com.management.chat_service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    // Exchange
    public static final String CHAT_EXCHANGE = "chat.exchange";

    // Queues
    public static final String CHAT_QUEUE = "chat.queue.user";
    public static final String GUEST_CHAT_QUEUE = "chat.queue.guest";
    public static final String AI_QUEUE = "chat.queue.ai";
    public static final String RESPONSE_QUEUE = "chat.queue.response";
    public static final String SESSION_CONVERT_QUEUE = "chat.queue.session.convert";
    public static final String USER_TO_USER_QUEUE = "chat.queue.user2user";

    // Routing keys
    public static final String CHAT_ROUTING_KEY = "chat.routing.user";
    public static final String GUEST_CHAT_ROUTING_KEY = "chat.routing.guest";
    public static final String AI_ROUTING_KEY = "chat.routing.ai";
    public static final String RESPONSE_ROUTING_KEY = "chat.routing.response";
    public static final String CONVERT_SESSION_ROUTING_KEY = "chat.routing.convert";
    public static final String USER_TO_USER_ROUTING_KEY = "chat.routing.user2user";

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Queue chatQueue() {
        return  QueueBuilder.durable(CHAT_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();

    }

    @Bean
    public Queue guestChatQueue() {
        return QueueBuilder.durable(GUEST_CHAT_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Queue aiQueue() {
        return  QueueBuilder.durable(AI_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Queue responseQueue() {
        return QueueBuilder.durable(RESPONSE_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Queue sessionConversionQueue() {
        return QueueBuilder.durable(SESSION_CONVERT_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(CHAT_ROUTING_KEY);
    }

    @Bean
    public Binding guestChatBinding() {
        return BindingBuilder.bind(guestChatQueue()).to(chatExchange()).with(GUEST_CHAT_ROUTING_KEY);
    }

    @Bean
    public Binding aiBinding() {
        return BindingBuilder.bind(aiQueue()).to(chatExchange()).with(AI_ROUTING_KEY);
    }

    @Bean
    public Binding responseBinding() {
        return BindingBuilder.bind(responseQueue()).to(chatExchange()).with(RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding convertSessionBinding() {
        return BindingBuilder.bind(sessionConversionQueue()).to(chatExchange()).with(CONVERT_SESSION_ROUTING_KEY);
    }

    @Bean
    public Queue userToUserQueue() {
        return QueueBuilder.durable(USER_TO_USER_QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Binding userToUserBinding() {
        return BindingBuilder.bind(userToUserQueue()).to(chatExchange()).with(USER_TO_USER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}