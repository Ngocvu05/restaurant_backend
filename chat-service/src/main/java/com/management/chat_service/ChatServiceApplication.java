package com.management.chat_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.management.chat_service")
@EnableJpaRepositories(basePackages = "com.management.chat_service.repository")
@EntityScan(basePackages = "com.management.chat_service.model")
@EnableDiscoveryClient
@EnableAsync
@EnableRabbit
public class ChatServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}
}
