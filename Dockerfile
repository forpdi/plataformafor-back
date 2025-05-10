FROM gradle:7.6-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle gradle/ .

RUN gradle dependencies || true

COPY . .

RUN ./gradlew build -x test

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/platfor.jar platfor.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT java $JAVA_OPTS -Duser.timezone=America/Sao_Paulo -jar /app/platfor.jar
