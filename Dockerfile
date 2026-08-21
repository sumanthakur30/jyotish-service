# Multi-stage build for optional docker compose profile "jyotish"
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/jyotish-service-0.0.1-SNAPSHOT.jar app.jar
# Optional Swiss Ephemeris (accuracy track): copy Thomas Mack JAR + set
#   JYOTISH_EPHEMERIS_PROVIDER=SWISS
#   JYOTISH_SWISS_JAR_PATH=/opt/swiss/swisseph.jar
# See third_party/swiss-ephemeris/README.md — default remains MEEUS (no JAR required).
EXPOSE 8097
ENTRYPOINT ["java","-jar","/app/app.jar"]
