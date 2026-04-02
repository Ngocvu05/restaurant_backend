package com.management.chat_service.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class EnvConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        return dotenv.get(key, System.getenv(key));
    }
}