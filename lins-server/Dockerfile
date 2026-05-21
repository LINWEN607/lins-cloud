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
EXPOSE 9999
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
