FROM maven:3.8-openjdk-8-slim AS builder
COPY settings.xml /usr/share/maven/conf/settings.xml
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:8-jre
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates curl \
    && curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-24.0.7.tgz | tar xz -C /usr/local/bin --strip-components=1 docker/docker \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /build/target/wgcloud-agent-release.jar app.jar
EXPOSE 9998
ENTRYPOINT ["java", "-jar", "app.jar"]
