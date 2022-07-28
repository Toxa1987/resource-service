FROM openjdk:17-alpine3.14

ARG JAR_FILE=target/resource-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar","/app.jar"]