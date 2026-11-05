# O Render não executa Java de forma nativa, então a API sobe em um contêiner.
FROM maven:3.9-eclipse-temurin-21 AS construcao
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Os testes rodam antes de publicar, usando H2. Não precisam do Oracle.
RUN mvn -q -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=construcao /app/target/varzea-api.jar app.jar
USER 1000
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
