# Stage 1: Build Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY ai-support-chatbot/frontend/package*.json ./
RUN npm install
COPY ai-support-chatbot/frontend/ ./
RUN npm run build

# Stage 2: Build Backend
FROM maven:3.8.5-eclipse-temurin-17 AS backend-build
WORKDIR /app
# Copy React build to backend static resources
COPY --from=frontend-build /app/frontend/dist ./ai-support-chatbot/backend/src/main/resources/static
COPY ai-support-chatbot/backend/pom.xml ./ai-support-chatbot/backend/
COPY ai-support-chatbot/backend/src ./ai-support-chatbot/backend/src
WORKDIR /app/ai-support-chatbot/backend
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/ai-support-chatbot/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
