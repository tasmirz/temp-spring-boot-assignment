FROM openjdk:25-ea-jdk
WORKDIR /app
EXPOSE 8080
# assignment1-0.0.1-SNAPSHOT.jar
COPY target/assignment1-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-jar", "assignment1-0.0.1-SNAPSHOT.jar"]