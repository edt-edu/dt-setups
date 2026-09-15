This project was mainly worked on using IntelliJ IDEA from Jet-Brains, namely the 2022 and 2023 versions. It is recommended to use IntelliJ as well, but not necessary. 

Here are the versions of the technologies used in the project:

kotlinc-jvm: 1.7.21    
JRE: 17.0.7+7-LTS
Maven Version: 3.9.5 
Docker Version: 24.0.2


Docker example script:

````
# Part 1: Build the app using Maven
FROM maven:3.6.0-jdk-8-alpine

## download dependencies
ADD pom.xml /
RUN mvn verify clean
## build after dependencies are down so it wont redownload unless the POM changes
ADD ./ /
RUN mvn package

# Part 2: use the JAR file used in the first part and copy it across ready to RUN
FROM openjdk:8-jdk-alpine
WORKDIR /root/

## COPY packaged JAR file and rename as app.jar
## → this relies on your MAVEN package command building a jar
## that matches *-jar-with-dependencies.jar with a single match
COPY --from=0 /target/*-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]

`````

Docker compose example Script:

````
version: '3.9'

services:
  # DT
  digitaltwin:
    container_name: digitalTwin
    hostname: localhost
    build:
      context: ./
      dockerfile: Dockerfile
    image: digitaltwin:latest
    ports:
      - "5000:80"
````