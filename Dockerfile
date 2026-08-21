# Multi-stage build for optional docker compose profile "jyotish"
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/jyotish-service-0.0.1-SNAPSHOT.jar app.jar
# Optional Swiss Ephemeris (accuracy track):
#   1) Download JAR:  pwsh scripts/download-swiss-jar.ps1
#   2) Optional .se1: pwsh scripts/download-swiss-ephe.ps1
#   3) Mount volume:  -v ./third_party/swiss-ephemeris/ephe:/opt/swiss/ephe:ro
#   4) Env:
#        JYOTISH_EPHEMERIS_PROVIDER=SWISS
#        JYOTISH_SWISS_JAR_PATH=/opt/swiss/swisseph.jar
#        JYOTISH_SWISS_EPHE_PATH=/opt/swiss/ephe
#        JYOTISH_SWISS_USE_FILES=true
# Default remains MEEUS (no JAR / .se1 required). See third_party/swiss-ephemeris/LICENSE-DECISION.md.
EXPOSE 8097
ENTRYPOINT ["java","-jar","/app/app.jar"]
