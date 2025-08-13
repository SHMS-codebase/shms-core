# 🌱 Use a lightweight Java 21 JRE base image
FROM eclipse-temurin:21-jre-alpine

# 👤 Create dedicated app user and group
RUN addgroup -S spring && adduser -S spring -G spring

# 📁 Create log directory and set ownership
RUN mkdir -p /opt/logs && chown -R spring:spring /opt/logs

# 🚚 Copy the JAR file to the container and set permissions
COPY target/SmartHealthCareMngnt-0.0.1-SNAPSHOT.jar /opt/app.jar
RUN chown spring:spring /opt/app.jar && chmod 755 /opt/app.jar

# ⚙️ Set working directory
WORKDIR /opt

# 🛠️ Switch to root temporarily to install MySQL client (for debugging)
USER root
RUN apk add --no-cache mysql-client

# 🔐 Switch back to non-root user for app execution
USER spring

# 🌐 Expose Spring Boot’s default port
EXPOSE 8080

# 🚀 Launch the app in foreground
ENTRYPOINT ["java", "-jar", "app.jar"]