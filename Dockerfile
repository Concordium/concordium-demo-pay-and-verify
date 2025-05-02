FROM adoptopenjdk/openjdk11:jre
COPY /build/libs/application.jar /application.jar
CMD ["java", "-jar", "/application.jar", "-Dlog4j.configuration=file:/log4j.properties"]
