FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/ecommerce-store-application.jar
COPY docker-utilities/wait-for-postgres.sh /app/wait-for-postgres.sh
COPY docker-utilities/launch-application.sh /app/launch-application.sh
RUN ["chmod", "+x", "/app/wait-for-postgres.sh"]
RUN ["chmod", "+x", "/app/launch-application.sh"]
RUN apk add postgresql-client
WORKDIR /app

EXPOSE 8080
