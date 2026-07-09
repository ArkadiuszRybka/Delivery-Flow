# syntax=docker/dockerfile:1
# Build context is the repo root (Maven module needs the parent pom.xml).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY notification-service/pom.xml notification-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f notification-service/pom.xml -B dependency:go-offline

COPY notification-service/src notification-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -f notification-service/pom.xml -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /build/notification-service/target/*.jar app.jar
USER spring

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
