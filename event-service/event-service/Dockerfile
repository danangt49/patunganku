FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 9002
ENTRYPOINT ["java", "-Duser.timezone=Asia/Jakarta", "-jar", "event-service-1.0.0.jar"]