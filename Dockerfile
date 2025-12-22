FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=CallCard_Server_WS/target/*.war
COPY ${JAR_FILE} app.war
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]
