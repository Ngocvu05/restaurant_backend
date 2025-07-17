package com.restaurant.chat_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableRabbit
@EntityScan("com.restaurant.chat_service.model")
@EnableJpaRepositories("com.restaurant.chat_service.repository")
public class ChatServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}
}
