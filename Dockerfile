FROM openjdk:11-jre-slim

EXPOSE 8080

COPY ./build/libs/*.jar /app/refinedPasswordManager.jar

WORKDIR /app

ENTRYPOINT ["java", "-jar", "/app/refinedPasswordManager.jar"]