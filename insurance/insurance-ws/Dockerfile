# Start with a base image containing Java runtime
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled jar file into the container
# Make sure you run './gradlew :insurance:insurance-ws:bootJar' first!
COPY build/libs/*-SNAPSHOT.jar app.jar

# Inform Docker that the container listens on port 8080
EXPOSE 8080

# The command to execute when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
