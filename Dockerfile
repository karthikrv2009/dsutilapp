# Step 1: Build the Spring Boot backend
FROM maven:3.8.5-openjdk-17 as backend-builder
WORKDIR /app/backend
COPY backend /app/backend
RUN mvn clean package -DskipTests

# Step 2: Build the React frontend
FROM node:18 as frontend-builder
WORKDIR /app/frontend
COPY frontend /app/frontend
RUN npm install
RUN npm run build

# Step 3: Combine builds into a single image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-builder /app/backend/target/*.jar app.jar
COPY --from=frontend-builder /app/frontend/build /app/static

# Step 4: Configure Spring Boot to serve the React build
# Optional: Adjust the "static" folder in application.properties if required

# Step 5: Expose the port and run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]