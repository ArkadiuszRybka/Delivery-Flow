# syntax=docker/dockerfile:1
# Build context is the repo root (Maven module needs the parent pom.xml).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f api-gateway/pom.xml -B dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN --mount=type=cache,target=/root/.m2 mvn -f api-gateway/pom.xml -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar app.jar
USER spring

# 8080: public API. 8090: actuator/metrics only, keep ClusterIP-internal in K8s (see Helm Service in Sekcja B).
EXPOSE 8080 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
