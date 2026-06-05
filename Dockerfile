FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package dependency:copy-dependencies -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/mukhametdinov-final4-1.0.jar ./app.jar
COPY --from=build /app/target/dependency ./dependency
ENTRYPOINT ["java", "-cp", "app.jar:dependency/*", "com.javarush.Main"]