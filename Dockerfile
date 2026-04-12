# ── Stage 1: Build Vue frontend ──────────────────────────────────────────────
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend
COPY src/frontend/package*.json ./
RUN npm ci
COPY src/frontend/ ./
RUN npm run build-only

# ── Stage 2: Build Spring Boot backend ───────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-build

WORKDIR /app/backend
COPY src/backend/pom.xml ./
COPY src/backend/src ./src

# Copy Vue dist into Spring Boot static resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static/

RUN apk add --no-cache maven
RUN mvn --batch-mode package -DskipTests

# ── Stage 3: Runtime image ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar"]
