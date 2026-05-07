# Start with a base image containing Java runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Remember to compile the JAR first using:
# ./gradlew :notification:notification-ws:bootJar
COPY build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
