FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
WORKDIR /workspace/services/api-gateway
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]