FROM openjdk:16-alpine

EXPOSE 8075

ADD target/my-cloud-0.0.1-SNAPSHOT.jar cloud.jar

ENTRYPOINT ["java","-jar","/cloud.jar"]