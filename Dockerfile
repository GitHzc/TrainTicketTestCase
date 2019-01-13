FROM java:8-jre


ADD ./target/ts-service-test-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx1000m", "-jar", "/app/ts-service-test-0.0.1-SNAPSHOT.jar"]

EXPOSE 10101