# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml và tải dependencies (tận dụng cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src và build
COPY src ./src
# Thêm lệnh build rõ ràng
RUN mvn clean package -DskipTests
# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ép kiểu UTF-8 chuẩn cho Alpine Linux
ENV LANG c.UTF-8
ENV LC_ALL c.UTF-8

# Bảo mật: Tạo user spring
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy file jar chính xác (tránh copy nhầm nhiều file jar nếu có)
COPY --from=build /app/target/InventorySystem-0.0.1-SNAPSHOT.jar app.jar

# Tối ưu JVM: Thêm thông số để tránh container bị kill do thiếu RAM (OOM)
ENTRYPOINT ["java", "-XX:+UseParallelGC", "-Xms256m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

EXPOSE 8080