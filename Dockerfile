FROM openjdk:17-jdk-alpine
LABEL maintainer="renanrocha8897@gmail.com"
RUN mkdir /app
RUN apk add maven
WORKDIR /app
COPY . .
RUN mvn clean install
# RUN mvn spring-boot:run
# ENTRYPOINT ["java", "-jar", "/app/app.jar"]
