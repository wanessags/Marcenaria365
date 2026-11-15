FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Extrai o jar antes de iniciar: evita ler classes de dentro de um jar aninhado
# de ~59 MB a cada inicialização. Medição local: ~20% menos tempo até a API responder.
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/extracted/app.jar"]
