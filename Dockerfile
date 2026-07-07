# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and build files first (better layer caching)
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Copy source
COPY src ./src

# Build boot jar
RUN ./gradlew bootJar -x test --no-daemon

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV PORT=8080

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Start app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]