FROM openjdk:8-jre
WORKDIR /app
COPY target/*-all.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--mongo-host", "mongodb", "--mongo-port", "27017", "--db-name", "inventorydb", "--db-collection", "items", "--headless"]