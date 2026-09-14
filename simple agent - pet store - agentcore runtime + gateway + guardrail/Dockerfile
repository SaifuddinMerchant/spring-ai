FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/simple-agent-pet-store-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]