FROM maven:3.8-openjdk-8-slim AS builder
COPY settings.xml /usr/share/maven/conf/settings.xml
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=builder /build/target/lins-server-release.jar app.jar
COPY application.yml.default /app/application.yml.default
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 9999
ENTRYPOINT ["/app/entrypoint.sh"]


