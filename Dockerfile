# Etapa de build
FROM maven:3.8.6-eclipse-temurin-17 AS build

# Define o diretório de trabalho
WORKDIR /home/app

# Copia o código-fonte e o arquivo pom.xml para o container
COPY src /home/app/src
COPY pom.xml /home/app

# Executa o Maven para construir o projeto
RUN mvn -f /home/app/pom.xml clean package

# Etapa de runtime
FROM openjdk:17-jdk-slim as grpc_service

# Copia o arquivo JAR gerado da etapa de build
COPY --from=build /home/app/target/file-grpc-service-1.0-SNAPSHOT.jar /usr/local/lib/file-grpc-service.jar

# Exponha a porta usada pelo gRPC
EXPOSE 50051

# Define o comando de entrada para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/usr/local/lib/file-grpc-service.jar"]

FROM envoyproxy/envoy:v1.19.1 AS envoy
COPY envoy.yaml /etc/envoy/envoy.yaml
EXPOSE 8080
CMD ["envoy", "-c", "/etc/envoy/envoy.yaml", "--service-cluster", "grpc_service"]