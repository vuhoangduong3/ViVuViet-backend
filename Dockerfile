# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM postgres:16-bookworm
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless curl tini \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY --from=build /build/target/Vi-vu-Viet-0.0.1-SNAPSHOT.jar /app/app.jar
COPY docker/start.sh docker/healthcheck.sh /app/
RUN sed -i 's/\r$//' /app/*.sh && chmod 755 /app/*.sh
ENV PGDATA=/var/lib/vivuviet/postgres \
    SPRING_PROFILES_ACTIVE=docker \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0"
VOLUME ["/var/lib/vivuviet"]
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=5 \
    CMD ["/app/healthcheck.sh"]
ENTRYPOINT ["/usr/bin/tini", "--", "/app/start.sh"]
