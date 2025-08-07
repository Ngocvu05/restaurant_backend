package com.management.search_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQHealthIndicator implements HealthIndicator {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Health health() {
        try {
            // Simple connectivity check
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive(RabbitMQConfig.DISH_SEARCH_QUEUE);
                return null;
            });

            return Health.up()
                    .withDetail("status", "RabbitMQ is accessible")
                    .withDetail("queues", "All required queues are available")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "RabbitMQ connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
