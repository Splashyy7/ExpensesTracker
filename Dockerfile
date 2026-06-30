# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /data/backups && chown -R appuser:appgroup /data

COPY --from=build /app/target/ExpensesTracker-1.0-SNAPSHOT.jar app.jar

ENV EXPENSES_DATA_FILE=/data/despesas.json
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
