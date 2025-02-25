FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

HEALTHCHECK --interval=10s --timeout=3s --retries=3 CMD curl -f http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]