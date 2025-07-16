package com.management.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đường dẫn thật tới thư mục trên máy
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");
        String uploadPath = uploadDir.toUri().toString();
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(uploadPath);
    }
}
