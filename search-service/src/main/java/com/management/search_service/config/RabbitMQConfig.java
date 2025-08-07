package com.management.search_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    // Exchange names (phải giống với user-service)
    public static final String DISH_EXCHANGE = "dish.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String REVIEW_EXCHANGE = "review.exchange";

    // Queue names
    public static final String DISH_SEARCH_QUEUE = "dish.search.queue";
    public static final String USER_SEARCH_QUEUE = "user.search.queue";
    public static final String REVIEW_SEARCH_QUEUE = "review.search.queue";

    // Dead Letter Queues
    public static final String DISH_DLQ = "dish.search.dlq";
    public static final String USER_DLQ = "user.search.dlq";
    public static final String REVIEW_DLQ = "review.search.dlq";

    // Dead Letter Exchanges
    public static final String DISH_DLX = "dish.dlx";
    public static final String USER_DLX = "user.dlx";
    public static final String REVIEW_DLX = "review.dlx";

    // Routing keys
    public static final String DISH_ROUTING_KEY = "dish.*";
    public static final String USER_ROUTING_KEY = "user.*";
    public static final String REVIEW_ROUTING_KEY = "review.*";

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

    // ============ DEAD LETTER EXCHANGES ============
    @Bean
    public DirectExchange dishDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DISH_DLX)
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
    public DirectExchange reviewDeadLetterExchange() {
        return ExchangeBuilder.directExchange(REVIEW_DLX)
                .durable(true)
                .build();
    }

    // ============ MAIN QUEUES ============
    @Bean
    public Queue dishSearchQueue() {
        return QueueBuilder.durable(DISH_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", DISH_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    @Bean
    public Queue userSearchQueue() {
        return QueueBuilder.durable(USER_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .withArgument("x-message-ttl", 300000)
                .build();
    }

    @Bean
    public Queue reviewSearchQueue() {
        return QueueBuilder.durable(REVIEW_SEARCH_QUEUE)
                .withArgument("x-dead-letter-exchange", REVIEW_DLX)
                .withArgument("x-dead-letter-routing-key", "failed")
                .withArgument("x-message-ttl", 300000)
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

    // ============ BINDINGS FOR MAIN QUEUES ============
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

    // ============ BINDINGS FOR DEAD LETTER QUEUES ============
    @Bean
    public Binding dishDeadLetterBinding() {
        return BindingBuilder.bind(dishDeadLetterQueue())
                .to(dishDeadLetterExchange())
                .with("failed");
    }

    @Bean
    public Binding userDeadLetterBinding() {
        return BindingBuilder.bind(userDeadLetterQueue())
                .to(userDeadLetterExchange())
                .with("failed");
    }

    @Bean
    public Binding reviewDeadLetterBinding() {
        return BindingBuilder.bind(reviewDeadLetterQueue())
                .to(reviewDeadLetterExchange())
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
                System.err.println("Message not delivered: " + cause);
            }
        });
        return template;
    }

    // ============ RETRY CONFIGURATION ============
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry policy: retry 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMaxInterval(10000);    // 10 seconds
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    // ============ MESSAGE RECOVERER ============
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DISH_DLX, "failed");
    }

    // ============ LISTENER CONTAINER FACTORY ============
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryTemplate retryTemplate,
            MessageRecoverer messageRecoverer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // Concurrency configuration
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(1);

        // Retry configuration
        factory.setRetryTemplate(retryTemplate);

        // Acknowledgment configuration
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}
