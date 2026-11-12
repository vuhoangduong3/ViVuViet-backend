# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
# Resolve dependencies in a separate layer so source edits do not invalidate it.
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY --from=build /build/target/Vi-vu-Viet-0.0.1-SNAPSHOT.jar /app/app.jar
COPY docker/healthcheck.sh /app/healthcheck.sh
RUN sed -i 's/\r$//' /app/healthcheck.sh && chmod 755 /app/healthcheck.sh
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0"
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=5 \
    CMD ["/app/healthcheck.sh"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
