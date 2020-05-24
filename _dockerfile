# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# The application's jar file
ARG JAR_FILE=target/spring-boot-fool-web-socket-1.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} fool-websocket.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/fool-websocket.jar"]
