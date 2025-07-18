package com.management.chat_service.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // Không lỗi nếu chưa có .env
            .load();

    public static String get(String key) {
        return dotenv.get(key, System.getenv(key)); // Ưu tiên file .env, fallback sang env hệ thống
    }
}
