# ---- Build stage ----
FROM gradle:8.8.0-jdk21 AS build

RUN apt-get update && apt-get install -y git

# Clone the master branch of your repository (replace URL as needed)

RUN git clone URLHERE /home/gradle/app

WORKDIR /home/gradle/app/app

# Build the application
RUN gradle build --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /home/gradle/app/app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]