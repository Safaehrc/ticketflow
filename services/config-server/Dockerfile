FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
WORKDIR /workspace/services/config-server
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]