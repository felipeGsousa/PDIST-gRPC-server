# Etapa de construção
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copie o arquivo pom.xml e as dependências para o contexto de construção
COPY pom.xml .
RUN mvn dependency:go-offline

# Copie o restante do código do aplicativo
COPY src ./src

# Compile o aplicativo
RUN mvn clean package -DskipTests

# Etapa de execução
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copie o arquivo JAR da etapa de construção
COPY --from=build /app/target/*.jar app.jar

# Exponha a porta que o serviço irá escutar
EXPOSE 50051

# Comando para iniciar o aplicativo
ENTRYPOINT ["java", "-jar", "app.jar"]