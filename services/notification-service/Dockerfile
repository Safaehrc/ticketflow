FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
WORKDIR /workspace/services/notification-service
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]