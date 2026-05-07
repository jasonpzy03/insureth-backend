# Start with a base image containing Java runtime
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# The application's compiled jar file is created in the build/libs folder after running gradle build
# We copy it into the container and rename it to app.jar
COPY build/libs/*-SNAPSHOT.jar app.jar

# Inform Docker that the container listens on port 8080 (the default Spring Boot port)
EXPOSE 8080

# The command to execute when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
