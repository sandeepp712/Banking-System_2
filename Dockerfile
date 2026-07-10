# --- STAGE 1: BUILD ---
# Use a heavy JDK image to compile the code
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies (Docker layer caching trick!)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the jar
COPY src ./src
RUN mvn package -DskipTests -B


# --- STAGE 2: RUN ---
# Use a lightweight JRE image to run the app (Much smaller!)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy ONLY the compiled jar from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Spring Boot uses
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
