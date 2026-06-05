FROM mysql:8.0

ENV MYSQL_DATABASE=ssafy_yumyumcoach
ENV MYSQL_USER=ssafy
ENV MYSQL_PASSWORD=ssafy
ENV MYSQL_ROOT_PASSWORD=ssafy
ENV TZ=Asia/Seoul

COPY src/main/resources/SSAFY_COACH_Schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY src/main/resources/SSAFY_COACH_Demo_Dump.sql /docker-entrypoint-initdb.d/02-demo-data.sql

EXPOSE 3306
