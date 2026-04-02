package com.management.search_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    // ============ CONSTANTS ============
    public static final String DISH_EXCHANGE = "dish.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String BOOKING_EXCHANGE = "booking.exchange";

    public static final String DISH_SEARCH_QUEUE = "dish.search.queue";
    public static final String USER_SEARCH_QUEUE = "user.search.queue";
    public static final String REVIEW_SEARCH_QUEUE = "review.search.queue";
    public static final String BOOKING_SEARCH_QUEUE = "booking.search.queue";

    public static final String DISH_DLQ = "dish.search.dlq";
    public static final String USER_DLQ = "user.search.dlq";
    public static final String REVIEW_DLQ = "review.search.dlq";
    public static final String BOOKING_DLQ = "booking.search.dlq";

    public static final String DISH_DLX = "dish.exchange.dlx";
    public static final String USER_DLX = "user.exchange.dlx";
    public static final String REVIEW_DLX = "review.exchange.dlx";
    public static final String BOOKING_DLX = "booking.exchange.dlx";

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

    // ============ DEAD LETTER EXCHANGES ============
    @Bean
    public DirectExchange dishDLX() {
        return ExchangeBuilder.directExchange(DISH_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange userDLX() {
        return ExchangeBuilder.directExchange(USER_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange reviewDLX() {
        return ExchangeBuilder.directExchange(REVIEW_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange bookingDLX() {
        return ExchangeBuilder.directExchange(BOOKING_DLX)
                .durable(true)
                .build();
    }

    // ============ MAIN QUEUES (WITHOUT TTL) ============
    @Bean
    public Queue dishSearchQueue() {
        return QueueBuilder.durable(DISH_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", DISH_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .build();
    }

    @Bean
    public Queue userSearchQueue() {
        return QueueBuilder.durable(USER_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .build();
    }

    @Bean
    public Queue reviewSearchQueue() {
        return QueueBuilder.durable(REVIEW_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", REVIEW_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .build();
    }

    @Bean
    public Queue bookingSearchQueue() {
        return QueueBuilder.durable(BOOKING_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", BOOKING_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .build();
    }

    // ============ DEAD LETTER QUEUES ============
    @Bean
    public Queue dishDeadLetterQueue() {
        return QueueBuilder.durable(DISH_DLQ).build();
    }

    @Bean
    public Queue userDeadLetterQueue() {
        return QueueBuilder.durable(USER_DLQ).build();
    }

    @Bean
    public Queue reviewDeadLetterQueue() {
        return QueueBuilder.durable(REVIEW_DLQ).build();
    }

    @Bean
    public Queue bookingDeadLetterQueue() {
        return QueueBuilder.durable(BOOKING_DLQ).build();
    }

    // ============ BINDINGS FOR MAIN QUEUES ============
    @Bean
    public Binding dishSearchBinding() {
        return BindingBuilder.bind(dishSearchQueue())
                .to(dishExchange())
                .with("dish.*");
    }

    @Bean
    public Binding userSearchBinding() {
        return BindingBuilder.bind(userSearchQueue())
                .to(userExchange())
                .with("user.*");
    }

    @Bean
    public Binding reviewSearchBinding() {
        return BindingBuilder.bind(reviewSearchQueue())
                .to(reviewExchange())
                .with("review.*");
    }

    @Bean
    public Binding bookingSearchBinding() {
        return BindingBuilder.bind(bookingSearchQueue())
                .to(bookingExchange())
                .with("booking.*");
    }

    // ============ BINDINGS FOR DEAD LETTER QUEUES ============
    @Bean
    public Binding dishDLQBinding() {
        return BindingBuilder.bind(dishDeadLetterQueue())
                .to(dishDLX())
                .with("failed");
    }

    @Bean
    public Binding userDLQBinding() {
        return BindingBuilder.bind(userDeadLetterQueue())
                .to(userDLX())
                .with("failed");
    }

    @Bean
    public Binding reviewDLQBinding() {
        return BindingBuilder.bind(reviewDeadLetterQueue())
                .to(reviewDLX())
                .with("failed");
    }

    @Bean
    public Binding bookingDLQBinding() {
        return BindingBuilder.bind(bookingDeadLetterQueue())
                .to(bookingDLX())
                .with("failed");
    }

    // ============ MESSAGE CONVERTER ============
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    // ============ RABBIT TEMPLATE ============
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("❌ Message not delivered: " + cause);
            }
        });
        return template;
    }

    // ============ RETRY TEMPLATE ============
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Exponential backoff: 1s, 2s, 4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    // ============ LISTENER CONTAINER FACTORY ============
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryTemplate retryTemplate) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // Concurrency
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(1);

        // Retry
        factory.setRetryTemplate(retryTemplate);

        // Acknowledgment
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false); // Go to DLQ on failure

        return factory;
    }
}