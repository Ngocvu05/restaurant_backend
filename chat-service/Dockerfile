# Dockerfile for Gradle-based Spring Boot app
FROM openjdk:21
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]