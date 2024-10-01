FROM maven:3.8.6-eclipse-temurin-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Etapa de runtime
FROM openjdk:17-jdk-slim
COPY --from=build /home/app/target/my-grpc-server.jar /usr/local/lib/my-grpc-server.jar
EXPOSE 50051
ENTRYPOINT ["java", "-jar", "/usr/local/lib/my-grpc-server.jar"]