# Build the frontend
FROM node:lts-alpine AS frontend-build
WORKDIR /workspace/frontend

COPY frontend/package.json .
COPY frontend/package-lock.json .
ENV NODE_ENV=development
RUN npm install
COPY frontend .
RUN npm run type-check && npm run build-only

# Build the API
FROM eclipse-temurin:21-jdk-alpine AS api-build
WORKDIR /workspace/api

COPY api/mvnw .
COPY api/.mvn .mvn
COPY api/pom.xml .
COPY api/src src
# Include the frontend in the jar, so we can serve it without an extra server
COPY --from=frontend-build /workspace/frontend/dist src/main/resources/static

RUN chmod +x mvnw && ./mvnw install -DskipTests

# Package everything together in a small image
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY --from=api-build /workspace/api/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
