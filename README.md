# YumYumCoach VueYum

YumYumCoach 프로젝트의 기존 JSP 사용자 화면을 **Vue 3 + Vite SPA**로 전환한 버전입니다.

기존 README가 Spring Batch 기반 영양성분 데이터 적재 기능을 중심으로 설명했다면, 이 README는 `origin/master` 이후 `vueyum`에서 진행한 JSP 화면의 Vue 전환, REST API 보강, Spring 정적 리소스 연동, 개발/배포 실행 구조 변경을 중심으로 정리합니다.

## 프로젝트 개요

YumYumCoach는 사용자의 식단 기록과 음식 영양 정보를 관리하는 Spring Boot 기반 애플리케이션입니다.

이번 변경의 핵심은 일반 사용자 화면을 기존 JSP에서 `client` 디렉터리의 Vue 3 + Vite SPA로 전환한 것입니다. Spring Boot는 `pnpm build`로 생성된 `src/main/resources/static/index.html`을 `/`, `/home`, `/auth/*`, `/meals*`, `/profile`, `/coach`, `/community`, `/challenges`, `/social` 경로에 forward합니다. 기존 JSP 화면 컨트롤러와 JSP 파일은 레거시 확인용으로 `/legacy/**` 경로에 남아 있습니다.

Spring Batch 기반 영양성분 데이터 적재 기능은 유지됩니다. 외부 영양성분 원천 데이터 파일을 읽어 staging 테이블에 저장한 뒤 정제와 검증을 거쳐 `food_nutrition` 테이블에 upsert합니다.

> [!NOTE]
> 외부 영양성분 데이터 출처: [식품의약품안전처 식품영양성분데이터베이스](https://various.foodsafetykorea.go.kr/nutrient/)

## 주요 변경점

- **Vue SPA 전환**
  - `client` 디렉터리에 Vue 3 + Vite + TypeScript 프로젝트를 추가했습니다.
  - `client/src/views`의 Vue 화면이 기존 JSP 사용자 화면을 대체합니다.
  - 공통 레이아웃, Router, Pinia 세션 store, alert 상태를 Vue 기준으로 구성했습니다.
  - `client/src/composables`의 Axios API composable이 `/api/v1/**` REST API를 호출합니다.
  - 세션 기반 인증은 유지하며, JWT/token refresh/interceptor 기반 인증은 사용하지 않습니다.

- **JSP 화면 기능 이전**
  - 로그인, 회원가입, 로그아웃 화면을 Vue form으로 전환했습니다.
  - 식단 목록/상세/등록/수정/삭제 화면을 Vue Router와 route query 기반 흐름으로 전환했습니다.
  - 프로필 조회/수정, 계정 비활성화, 계정 삭제 화면을 Vue로 전환했습니다.
  - 홈/코치 대시보드, 커뮤니티, 챌린지, 소셜 화면을 Vue로 전환했습니다.

- **REST API 보강**
  - 홈/코치 대시보드 API를 Vue 화면에서 사용할 수 있는 JSON API로 추가했습니다.
  - 커뮤니티 게시글/댓글 CRUD API를 추가했습니다.
  - 챌린지 생성/참여/진행률 수정/탈퇴/삭제 API를 추가했습니다.
  - 소셜 팔로우/언팔로우, 추천 사용자, 팔로워/팔로잉, 리더보드 API를 추가했습니다.
  - 프로필 대시보드 응답 DTO를 보강했습니다.

- **Spring 연동과 레거시 분리**
  - `pnpm build` 산출물은 `src/main/resources/static`에 생성되어 Spring Boot에서 바로 서빙됩니다.
  - SPA route 새로고침을 위해 Spring MVC fallback을 `index.html`로 연결했습니다.
  - `/api/v1/**`, `/batch/**`, `/swagger-ui/**`, `/v3/api-docs/**`는 Vue fallback 대상에서 제외했습니다.
  - 기존 JSP MVC 컨트롤러는 `/legacy/**` 경로로 이동했습니다.

- **개발/실행 환경 정리**
  - Vite 개발 서버가 `/api`, `/batch`, `/swagger-ui`, `/v3/api-docs` 요청을 Spring Boot로 proxy합니다.
  - `client/Dockerfile`로 Vue 개발 서버 실행 환경을 분리했습니다.
  - `database/Dockerfile`로 MySQL 초기 스키마와 demo data 적재 환경을 구성했습니다.

```text
email: demo@yamyam.com
password: Demo1234!
```

## AS-IS

- 일반 사용자 화면이 JSP와 Spring MVC view controller에 묶여 있었습니다.
- 화면 이동, form submit, redirect 흐름이 서버 렌더링 중심이었습니다.
- 인증, 식단, 프로필 외 영역은 Vue에서 재사용할 JSON API가 부족했습니다.
- 홈, 코치, 커뮤니티, 챌린지, 소셜 기능 일부는 JSP 컨트롤러에 의존했습니다.
- SPA route 직접 접근과 새로고침을 처리하는 Spring fallback 구성이 없었습니다.
- 프론트엔드 개발 서버, Spring API 서버, DB 실행 환경이 분리되어 정리되지 않았습니다.

## TO-BE

- 일반 사용자 화면은 Vue 3 + Vite SPA가 담당합니다.
- Spring Boot는 SPA 정적 산출물과 `/api/v1/**` REST API를 함께 제공합니다.
- 기존 JSP 화면은 `/legacy/**` 경로에서 확인용으로만 유지합니다.
- Vue 화면은 Pinia 세션 store와 Axios composable을 통해 세션 기반 API를 호출합니다.
- 홈, 코치, 커뮤니티, 챌린지, 소셜 기능은 Vue에서 사용할 REST API로 보강합니다.
- 개발 중에는 Vite proxy로 Spring API를 호출하고, 운영 빌드는 Spring static resource로 서빙합니다.
- Vue 개발 서버와 MySQL 초기화용 Dockerfile을 제공해 실행 환경을 분리합니다.

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
│  ├─ ChallengeApiController.java
│  ├─ CommunityApiController.java
│  ├─ DashboardApiController.java
│  ├─ FoodApiController.java
│  ├─ HealthApiController.java
│  ├─ MealApiController.java
│  ├─ NutritionBatchApiController.java
│  ├─ SocialApiController.java
│  └─ UserApiController.java
├─ controller
│  ├─ SpaController.java
│  └─ *Controller.java  # /legacy/** JSP 확인용 MVC 컨트롤러
├─ repository
├─ service
├─ model
└─ config

client
├─ src/views
├─ src/composables
├─ src/router
├─ src/stores
└─ Dockerfile

database
└─ Dockerfile
```

## 실행 환경

- Java 21
- Spring Boot 3.5.14
- Spring Batch
- Maven
- MySQL
- Vue 3 + Vite
- pnpm
- JSP(`/legacy/**` 확인용)
- JDBC
- Swagger/OpenAPI
- Apache POI

## 실행 방법

1. MySQL에서 `ssafy_yumyumcoach` 스키마를 준비합니다.
  - `assets/ssafy_yumyumcoach.sql` 을 실행합니다.
2. `src/main/resources/application.properties`의 DB 계정 정보를 확인합니다.
3. Vue 정적 산출물을 생성합니다.

```sh
cd client
pnpm install
pnpm build
```

4. `YumyumApplication.java`를 실행합니다.
5. 브라우저에서 `http://localhost:8080` 또는 Swagger UI에 접속합니다.

개발 중에는 Spring Boot를 `http://localhost:8080`에서 실행한 뒤 별도 터미널에서 Vite 개발 서버를 사용할 수 있습니다.

```sh
cd client
pnpm dev
```

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

- [Vue 전환 구현 계획](./docs/PLAN.md)
- [Vue 활용 제한](./docs/LIMIT.md)
- [API 문서](./docs/API.md)
- [프로젝트 분석](./docs/RESEARCH.md)
- [DB 스키마](./assets/ssafy_yumyumcoach.sql)
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
