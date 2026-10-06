# =====================================================================
# Imagen para Render: compila con Maven y ejecuta con Java 17.
# =====================================================================

# --- Etapa 1: compilación ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Descarga dependencias una sola vez (se reutiliza si pom.xml no cambia)
RUN mvn -q dependency:go-offline
COPY src ./src
# Las pruebas usan mocks y ya se corren en la PC; aquí se omiten para acelerar
RUN mvn -q -DskipTests package

# --- Etapa 2: ejecución ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
# Hora de Paraguay para "hoy" y las fechas; memoria acotada al plan gratuito
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Asuncion -XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
