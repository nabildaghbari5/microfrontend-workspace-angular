# Base image for Spring Boot or Java
FROM openjdk:21-jdk-slim

# Add your application jar file
COPY target/profile-0.0.1-SNAPSHOT.jar /app/profile-service.jar

# Expose the port
EXPOSE 9999

# Run the app
ENTRYPOINT ["java", "-jar", "/app/profile-service.jar"]