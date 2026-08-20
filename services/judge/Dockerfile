# build from repo root: docker build -f services/judge/Dockerfile .
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY services/common/pom.xml services/common/pom.xml
COPY services/api/pom.xml services/api/pom.xml
COPY services/judge/pom.xml services/judge/pom.xml

COPY services/common services/common
COPY services/api services/api
COPY services/judge services/judge

RUN mvn -pl services/judge -am package -DskipTests

# JDK (not JRE): process sandbox needs javac; also install python3 + g++
FROM eclipse-temurin:21-jdk
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 g++ \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/services/judge/target/judge-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
