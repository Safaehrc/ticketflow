FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
WORKDIR /workspace/services/event-service
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/event-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]