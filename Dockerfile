# ---------- Build stage ----------
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# 의존성 먼저 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle --no-daemon dependencies || true

# 소스 복사 후 빌드
COPY src src
RUN gradle --no-daemon bootJar

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]