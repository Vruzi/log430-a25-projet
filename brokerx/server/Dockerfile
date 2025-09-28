FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon -x test
EXPOSE 8080
CMD ["./gradlew", "bootRun", "--no-daemon"]