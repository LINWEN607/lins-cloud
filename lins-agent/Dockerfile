FROM maven:3.8-openjdk-8-slim AS builder
COPY settings.xml /usr/share/maven/conf/settings.xml
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:8-jre
RUN apt-get update && apt-get install -y --no-install-recommends docker.io \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /build/target/lins-agent-release.jar app.jar
EXPOSE 9998
ENTRYPOINT ["java", "-jar", "app.jar"]

