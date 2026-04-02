package com.management.restaurant.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // ============ EXCHANGE NAMES ============
    public static final String DISH_EXCHANGE = "dish.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String BOOKING_EXCHANGE = "booking.exchange";

    // ============ MAIN EXCHANGES ============
    @Bean
    public TopicExchange dishExchange() {
        return ExchangeBuilder.topicExchange(DISH_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange(USER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange reviewExchange() {
        return ExchangeBuilder.topicExchange(REVIEW_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange bookingExchange() {
        return ExchangeBuilder.topicExchange(BOOKING_EXCHANGE)
                .durable(true)
                .build();
    }

    // ============ MESSAGE CONVERTER ============
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    // ============ RABBIT TEMPLATE ============
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // Enable publisher confirms
        template.setMandatory(true);

        // Confirm callback
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("❌ Message NOT delivered to exchange: " + cause);
            } else {
                System.out.println("✅ Message confirmed by exchange");
            }
        });

        // Return callback
        template.setReturnsCallback(returned -> {
            System.err.println("❌ Message RETURNED: " +
                    returned.getReplyText() +
                    " | Routing Key: " + returned.getRoutingKey());
        });

        return template;
    }
}