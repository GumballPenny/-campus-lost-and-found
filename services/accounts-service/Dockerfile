FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S campus && adduser -S campus -G campus
WORKDIR /app
COPY --from=build /build/target/campus-accounts-1.0-SNAPSHOT.jar app.jar
USER campus
EXPOSE 8081
HEALTHCHECK --interval=20s --start-period=45s --timeout=5s CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","app.jar"]
