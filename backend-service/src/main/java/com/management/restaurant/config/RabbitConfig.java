package com.management.restaurant.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // Exchange names
    public static final String DISH_EXCHANGE = "dish.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String BOOKING_EXCHANGE = "booking.exchange";

    // Queue names
    public static final String DISH_SEARCH_QUEUE = "dish.search.queue";
    public static final String USER_SEARCH_QUEUE = "user.search.queue";
    public static final String REVIEW_SEARCH_QUEUE = "review.search.queue";

    // ============ DEAD LETTER EXCHANGES ============
    public static final String BOOKING_DLX = "booking.dlx";
    public static final String USER_DLX = "user.dlx";
    public static final String DISH_DLX = "dish.dlx";
    public static final String REVIEW_DLX = "review.dlx";

    // Routing keys
    public static final String DISH_ROUTING_KEY = "dish.*";
    public static final String USER_ROUTING_KEY = "user.*";
    public static final String REVIEW_ROUTING_KEY = "review.*";

    // Exchanges
    @Bean
    public TopicExchange dishExchange() {
        return new TopicExchange(DISH_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange(REVIEW_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return ExchangeBuilder.topicExchange(BOOKING_EXCHANGE)
                .durable(true)
                .build();
    }

    // Queues
    @Bean
    public Queue dishSearchQueue() {
        return QueueBuilder.durable(DISH_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", DISH_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue userSearchQueue() {
        return QueueBuilder.durable(USER_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue reviewSearchQueue() {
        return QueueBuilder.durable(REVIEW_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", REVIEW_EXCHANGE + ".dlx")
                .build();
    }

    // ============ DEAD LETTER EXCHANGES ============
    @Bean
    public DirectExchange bookingDeadLetterExchange() {
        return ExchangeBuilder.directExchange(BOOKING_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange userDeadLetterExchange() {
        return ExchangeBuilder.directExchange(USER_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange dishDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DISH_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange reviewDeadLetterExchange() {
        return ExchangeBuilder.directExchange(REVIEW_DLX)
                .durable(true)
                .build();
    }

    // Bindings
    @Bean
    public Binding dishSearchBinding() {
        return BindingBuilder.bind(dishSearchQueue())
                .to(dishExchange())
                .with(DISH_ROUTING_KEY);
    }

    @Bean
    public Binding userSearchBinding() {
        return BindingBuilder.bind(userSearchQueue())
                .to(userExchange())
                .with(USER_ROUTING_KEY);
    }

    @Bean
    public Binding reviewSearchBinding() {
        return BindingBuilder.bind(reviewSearchQueue())
                .to(reviewExchange())
                .with(REVIEW_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // Enable publisher confirms
        template.setMandatory(true);

        // Confirm callback (when message reaches exchange)
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("❌ Message NOT delivered to exchange: " + cause);
            } else {
                System.out.println("✅ Message confirmed by exchange");
            }
        });

        // Return callback (when message cannot be routed to queue)
        template.setReturnsCallback(returned -> {
            System.err.println("❌ Message RETURNED: " +
                    returned.getReplyText() +
                    " | Message: " + returned.getMessage());
        });
        return template;
    }
}