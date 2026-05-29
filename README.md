# YumYumCoach BatchYum

YumYumCoach 프로젝트에 **Spring Batch 기반 영양성분 데이터 적재 기능**을 추가한 버전입니다.

기존 README가 Spring MVC/JSP 프로젝트를 REST API로 전환한 변경사항을 중심으로 설명했다면, 이 README는 `batchyum`에서 추가된 배치 처리, 데이터 적재, 리포트 생성, Swagger 테스트 편의 기능을 중심으로 정리합니다.

## 프로젝트 개요

YumYumCoach는 사용자의 식단 기록과 음식 영양 정보를 관리하는 Spring Boot 기반 애플리케이션입니다.

이번 변경의 핵심은 외부 영양성분 원천 데이터 파일을 읽어 YumYumCoach의 `food_nutrition` 테이블에 안정적으로 적재하는 배치 파이프라인입니다. 배치는 원본 데이터를 바로 최종 테이블에 넣지 않고, staging 테이블에 먼저 저장한 뒤 정제와 검증을 거쳐 upsert합니다.

> [!NOTE]
> 외부 영양성분 데이터 출처: [식품의약품안전처 식품영양성분데이터베이스](https://various.foodsafetykorea.go.kr/nutrient/)

## 주요 변경점

- **영양성분 데이터 적재**
  - Spring Batch를 사용해 외부 영양성분 원천 데이터를 YumYumCoach DB에 적재합니다.
  - 식품명, 분류, 중량, 열량, 단백질, 지방, 탄수화물, 당류, 나트륨 등 영양 정보를 `food_nutrition`에 저장합니다.
  - CSV와 XLSX 파일을 모두 처리할 수 있습니다.
  - 대용량 XLSX 파일은 Apache POI 이벤트 기반 스트리밍 방식으로 읽습니다.

- **배치 처리 파이프라인**
  - 처리 흐름은 `파일 읽기 -> staging 적재 -> 정제 -> food_nutrition upsert -> 리포트 생성`입니다.
  - 기본 chunk size는 100건입니다.
  - 원본 row는 `nutrition_import_staging`에 저장되어 처리 상태를 추적할 수 있습니다.
  - 정상 처리 row는 `DONE`, 실패 row는 `FAILED`로 기록됩니다.
  - 실패 row에는 오류 메시지를 남겨 재처리와 원인 분석이 가능하도록 했습니다.

- **중복 방지와 멱등 처리**
  - 최종 적재는 `food_code` 기준 upsert로 처리합니다.
  - 같은 데이터를 여러 번 실행해도 중복 row가 계속 쌓이지 않습니다.
  - 원본 식품코드가 있으면 그대로 사용합니다.
  - 식품코드가 없으면 `sourceName`, `foodName`, `category`, `weight`를 기반으로 안정적인 식별 코드를 생성합니다.

- **배치 실행과 운영**
  - 서버 시작 시 배치가 자동 실행되지 않습니다.
  - Swagger 또는 HTTP 요청으로 수동 실행합니다.
  - 배치 실행 시 `sourcePath`, `sourceName`, `chunkSize`, `runId`를 전달할 수 있습니다.
  - 실패 또는 중단된 배치는 `executionId` 기준으로 재시작할 수 있습니다.

- **PDF 리포트**
  - 배치 완료 후 PDF 리포트를 자동 생성합니다.
  - PDF에는 실행 ID, 원본 파일 정보, 총 처리 건수, 성공/실패/대기 건수, 실패 row 샘플이 포함됩니다.
  - 저장 경로는 다음과 같습니다.

```text
reports/batch/nutrition/nutrition-import-{jobExecutionId}.pdf
```

- **Swagger 테스트 편의**
  - Swagger UI에서 기존 REST API와 배치 API를 함께 확인할 수 있습니다.
  - 세션 인증용 `JSESSIONID` 쿠키 스키마를 문서화했습니다.
  - 로그인 API에는 demo 계정 요청 예시가 포함되어 있습니다.

```text
email: demo@yamyam.com
password: Demo1234!
```

## AS-IS

- 영양성분 데이터는 애플리케이션 DB에 수동으로 넣거나 별도 SQL dump에 의존해야 했습니다.
- CSV/XLSX 원천 데이터 파일을 반복적으로 읽고 정제하는 표준 파이프라인이 없었습니다.
- 대용량 영양성분 파일을 안전하게 나눠 처리하는 구조가 없었습니다.
- 중복 수집 방지, 실패 row 추적, 실행별 처리 결과 요약이 부족했습니다.
- 배치 실행 결과를 운영자가 확인할 수 있는 별도 리포트 산출물이 없었습니다.
- Swagger에는 일반 REST API 중심의 문서만 있었고, 배치 실행 흐름과 테스트 계정 안내가 부족했습니다.

## TO-BE

- Spring Batch Job/Step/Chunk 기반으로 영양성분 데이터를 안정적으로 적재합니다.
- 원본 데이터를 staging 테이블에 남겨 row 단위 처리 상태를 추적합니다.
- 정제 성공 데이터는 `food_nutrition`에 upsert하여 멱등성을 보장합니다.
- 정제 실패 데이터는 실패 상태와 오류 메시지를 남겨 재처리 근거로 사용합니다.
- 실행 결과는 `nutrition_import_report`에 저장하고 PDF 리포트로도 생성합니다.
- Swagger에서 demo 계정으로 로그인한 뒤 배치 실행 API를 바로 테스트할 수 있습니다.

## 배치 실행 API

Swagger UI:

- OpenAPI Spec: [openapi.json](./assets/openapi.json)
- Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

실행 결과:

![](./assets/ssafy_yumyumcoach.png)

### 실행 조건과 로그인

- 애플리케이션과 MySQL이 실행 중이어야 합니다.
- `sourcePath`는 서버가 접근할 수 있는 로컬 CSV/XLSX 파일 경로여야 합니다.
- 배치 API는 로그인된 세션에서 실행하는 것을 기준으로 합니다.
- Swagger에서 테스트할 때는 먼저 `Auth API > POST /api/v1/auth/login`을 실행하면 편합니다.

```json
{
  "email": "demo@yamyam.com",
  "password": "Demo1234!"
}
```

- 로그인 후 같은 Swagger UI 화면에서 배치 API를 실행하면 세션 쿠키가 함께 전달됩니다.
- `spring.batch.job.enabled=false` 설정 때문에 서버 시작만으로 배치가 자동 실행되지는 않습니다.

- Swagger UI 캡처:

![Swagger UI](./assets/swagger.png)

영양성분 배치 실행:

```http
GET /batch/nutrition-import?sourcePath=data/영양성분 DB/농림수산식품교육문화정보원_칼로리 정보_20190926.csv&chunkSize=100&runId=manual-001
```

주요 파라미터:

| 파라미터 | 설명 |
| --- | --- |
| `sourcePath` | 서버 로컬 기준 원본 CSV/XLSX 파일 경로 |
| `sourceName` | 리포트에 표시할 원본 이름. 생략 시 파일명 사용 |
| `chunkSize` | chunk 처리 단위. 기본값 100, 최대 500 |
| `runId` | Spring Batch JobInstance 식별용 실행 ID |

실패한 배치 재시작:

```http
GET /batch/nutrition-import/restart?executionId=123
```

응답 예시:

```json
{
  "executionId": 123,
  "status": "STARTING",
  "sourceName": "농림수산 칼로리 CSV",
  "pdfReportPath": "reports/batch/nutrition/nutrition-import-123.pdf"
}
```

### 리포트 확인 방법

- 배치가 완료되면 응답의 `pdfReportPath` 위치에 PDF 리포트가 생성됩니다.
- 기본 저장 경로는 다음과 같습니다.

```text
reports/batch/nutrition/nutrition-import-{jobExecutionId}.pdf
```

- PDF에는 실행 ID, 원본 파일 정보, 총 처리 건수, 성공/실패/대기 건수, 실패 row 샘플이 포함됩니다.
- DB에서는 `nutrition_import_report` 테이블로 실행별 요약을 확인할 수 있습니다.
- 실패 row 상세는 `nutrition_import_staging`에서 `import_status = 'FAILED'` 조건으로 확인할 수 있습니다.

## 주요 테이블

| 테이블 | 설명 |
| --- | --- |
| `food_nutrition` | 최종 영양성분 데이터 |
| `nutrition_import_staging` | 원본 row와 처리 상태 저장 |
| `nutrition_import_report` | 배치 실행별 처리 결과 요약 |
| `users` | 사용자 정보 |
| `diet_logs` | 사용자 식단 기록 |
| `diet_log_items` | 식단별 섭취 음식 목록 |

## 프로젝트 구조

```text
src/main/java/com/ssafy/yumyum
├─ batch/nutrition
│  ├─ CsvNutritionItemReader.java
│  ├─ XlsxNutritionItemReader.java
│  ├─ NutritionImportJobConfig.java
│  ├─ NutritionImportRepository.java
│  ├─ NutritionNormalizeProcessor.java
│  ├─ NutritionImportReportTasklet.java
│  └─ NutritionImportPdfReportTasklet.java
├─ controller/api
│  ├─ AuthApiController.java
│  ├─ FoodApiController.java
│  ├─ MealApiController.java
│  ├─ NutritionBatchApiController.java
│  └─ UserApiController.java
├─ repository
├─ service
├─ model
└─ config
```

## 실행 환경

- Java 21
- Spring Boot 3.5.14
- Spring Batch
- Maven
- MySQL
- JSP
- JDBC
- Swagger/OpenAPI
- Apache POI

## 실행 방법

1. MySQL에서 `ssafy_yumyumcoach` 스키마를 준비합니다.
  - `assets/ssafy_yumyumcoach.sql` 을 실행합니다.
2. `src/main/resources/application.properties`의 DB 계정 정보를 확인합니다.
3. `YumyumApplication.java`를 실행합니다.
4. 브라우저에서 `http://localhost:8080` 또는 Swagger UI에 접속합니다.

## application.properties 주요 설정

```properties
spring.application.name=springyum

server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/ssafy_yumyumcoach?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=ssafy
spring.datasource.password=ssafy
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always

springdoc.swagger-ui.with-credentials=true

server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

## 변경 시 주의할 점

- 기존 MySQL DB가 `users.user_id INT AUTO_INCREMENT`로 생성되어 있다면 스키마를 다시 생성해야 합니다.
  - 현재 프로젝트는 `user_demo` 같은 문자열 사용자 ID를 기준으로 통일했습니다.
  - `users.user_id`, `diet_logs.user_id`는 `VARCHAR(64)` 기준입니다.
- `reports/` 경로는 배치 실행 산출물이므로 Git에 포함하지 않습니다.
- 배치 metadata 테이블은 별도 DB가 아니라 기존 애플리케이션 DB에 생성됩니다.
- `spring.batch.job.enabled=false`이므로 서버 시작만으로 배치가 자동 실행되지 않습니다.
- 대용량 XLSX 파일은 먼저 작은 CSV로 검증한 뒤 실행하는 것을 권장합니다.

## 관련 파일

- [배치 구현 계획](./docs/PLAN.md)
- [교육 내용 정리](./docs/EDUCATION.md)
- [브레인스토밍](./docs/BRAINSTROMING.md)
- [프로젝트 분석](./docs/RESEARCH.md)
- [DB 스키마](./assets/ssafy_yumyumcoach.sql)
- [데모 덤프](./assets/ssafy_yumyumcoach-demo-dump.sql)
- [ERD 이미지](./assets/ssafy_yumyumcoach.png)
- [Swagger 캡처](./assets/swagger.png)

---

## 다이어그램

### ERD

![ERD](./assets/ssafy_yumyumcoach.png)

### 클래스 다이어그램

![Class Diagram](./src/main/resources/class-diagram.jpeg)

## 실행 화면

### 메인 화면 / 소셜 화면

<p align="center">
  <img src="./src/main/resources/images/메인화면.png" alt="메인 화면" width="48%">
  <img src="./src/main/resources/images/소셜.png" alt="소셜 화면" width="48%">
</p>

### 식단 기록 / 커뮤니티 화면

<p align="center">
  <img src="./src/main/resources/images/식단기록.png" alt="식단 기록 화면" width="48%">
  <img src="./src/main/resources/images/커뮤니티.png" alt="커뮤니티 화면" width="48%">
</p>

### 챌린지 화면

<p align="center">
  <img src="./src/main/resources/images/챌린지.png" alt="챌린지 화면" width="60%">
</p>
