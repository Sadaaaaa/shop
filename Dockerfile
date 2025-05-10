FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY . .
RUN cd main_service && ./mvnw clean package spring-boot:repackage -DskipTests
RUN cd payment && ./mvnw clean package spring-boot:repackage -DskipTests
RUN cd auth_server && ./mvnw clean package spring-boot:repackage -DskipTests

# Main service image
FROM openjdk:21-jdk-slim AS main_service
WORKDIR /app
COPY --from=builder /app/main_service/target/main_service-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
# Устанавливаем переменные окружения для Redis без аутентификации
ENV REDIS_HOST=redis \
    REDIS_PORT=6379
ENTRYPOINT ["java", "-jar", "main_service-0.0.1-SNAPSHOT.jar"]

# Payment service image
FROM openjdk:21-jdk-slim AS payment
WORKDIR /app
COPY --from=builder /app/payment/target/payment-0.0.1-SNAPSHOT.jar .
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "payment-0.0.1-SNAPSHOT.jar"]

# Auth server image
FROM openjdk:21-jdk-slim AS auth_server
WORKDIR /app
COPY --from=builder /app/auth_server/target/auth_server-0.0.1-SNAPSHOT.jar .
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "auth_server-0.0.1-SNAPSHOT.jar"] 