FROM openjdk:17
ADD target/realTime-0.0.1-SNAPSHOT.jar realTime-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "realTime-0.0.1-SNAPSHOT.jar"]