FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
WORKDIR /workspace/services/booking-service
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/booking-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]