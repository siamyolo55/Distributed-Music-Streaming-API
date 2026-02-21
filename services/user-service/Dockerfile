FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl services/user-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/user-service/target/user-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
