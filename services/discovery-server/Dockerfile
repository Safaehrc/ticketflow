# Étape 1 : Build de l'application avec Maven en ignorant les tests
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copie de l'intégralité du projet et des sources
COPY . .

# Compilation spécifique du discovery-server
RUN mvn clean package -pl services/discovery-server -Dmaven.test.skip=true

# Étape 2 : Image finale légère pour l'exécution (JRE 21)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copie ciblée uniquement du jar du discovery-server
COPY --from=build /workspace/services/discovery-server/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]