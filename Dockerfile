FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
COPY src/ src/
RUN chmod +x gradlew && ./gradlew --no-daemon bootWar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/FridegeShare.war app.war
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.war"]