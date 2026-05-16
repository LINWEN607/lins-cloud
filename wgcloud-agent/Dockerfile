FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=builder /build/target/wgcloud-agent-release.jar app.jar
EXPOSE 9998
ENTRYPOINT ["java", "-jar", "app.jar"]
