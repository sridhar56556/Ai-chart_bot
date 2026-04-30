# Stage 1: Build Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./frontend/
RUN cd frontend && npm install
COPY frontend/ ./frontend/
RUN cd frontend && npm run build

# Stage 2: Build Backend
FROM maven:3.8.5-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
# Copy frontend build to static resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
