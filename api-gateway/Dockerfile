# Dockerfile for Gradle-based Spring Boot app
#FROM openjdk:21

FROM eclipse-temurin:21-jdk
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
