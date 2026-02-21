FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl services/media-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/media-service/target/media-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
