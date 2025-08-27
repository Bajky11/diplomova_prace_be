# Base image s JDK 17
FROM openjdk:17-slim AS build
WORKDIR /app

# Zkopíruj Maven Wrapper a nastav spustitelná práva
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN chmod +x mvnw

# Build aplikace
COPY src ./src
RUN ./mvnw package -DskipTests

# Spustitelný image pro produkci
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
# docker build -t friends-be .
# docker run -p 8080:8080 --name friends-be friends-be