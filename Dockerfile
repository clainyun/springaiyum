FROM node:24-alpine AS client-build

WORKDIR /workspace/client

ENV PNPM_HOME=/pnpm
ENV PATH="$PNPM_HOME:$PATH"

RUN corepack enable && corepack prepare pnpm@11.5.0 --activate

COPY client/package.json client/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY client ./
RUN pnpm build

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -DskipTests dependency:go-offline

COPY src ./src
COPY --from=client-build /workspace/src/main/resources/static ./src/main/resources/static
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SERVER_PORT=8080
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ssafy_yumyumcoach?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
ENV SPRING_DATASOURCE_USERNAME=ssafy
ENV SPRING_DATASOURCE_PASSWORD=ssafy
ENV SPRING_BATCH_JOB_ENABLED=false
ENV TZ=Asia/Seoul

COPY --from=build /workspace/target/springyum-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
