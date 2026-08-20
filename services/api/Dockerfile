# build from repo root: docker build -f services/api/Dockerfile .
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY services/common/pom.xml services/common/pom.xml
COPY services/api/pom.xml services/api/pom.xml
COPY services/judge/pom.xml services/judge/pom.xml

COPY services/common services/common
COPY services/api services/api
COPY services/judge services/judge

RUN mvn -pl services/api -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/api/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
