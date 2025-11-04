# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/SysOnBackendScript.py /app/src/main/resources/SysOnBackendScript.py
RUN apt-get update && apt-get install -y python3 python3-pip python3-requests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]