# YumYumCoach 기능 분석

이 문서는 `README.md`와 `./src` 경로의 실제 구현을 기준으로, 현재 프로젝트가 수행하는 기능과 구조를 분석한 결과다.

## 1. 프로젝트 개요

YumYumCoach는 사용자가 식단을 기록하고, 영양 섭취 상태를 확인하며, 커뮤니티/챌린지/소셜 기능을 통해 건강 관리 활동을 이어가도록 돕는 Spring Boot 기반 웹 애플리케이션이다.

기존 Servlet/JSP 프로젝트를 Spring Boot + Spring MVC 구조로 옮긴 뒤, 같은 기능을 `client` 디렉터리의 Vue 3 + Vite SPA에서도 사용할 수 있도록 `/api/v1` REST API를 확장한 상태다. 기존 JSP 화면 기반 MVC 기능도 아직 함께 남아 있다.

핵심 기능은 다음과 같다.

| 영역 | 수행 기능 |
| --- | --- |
| 인증 | 회원가입, 로그인, 로그아웃, 세션 기반 로그인 유지 |
| 사용자 | 프로필 조회/수정, 비밀번호 변경, 계정 비활성화 |
| 식단 | 식단 목록/상세/등록/수정/삭제, 음식 검색, 영양 계산 |
| 코칭 | 일일 목표, 최근 식단 분석, 운동/회복/다음 행동 제안 |
| 커뮤니티 | 게시글 CRUD, 댓글 CRUD, 카테고리 필터, 식단 연결 |
| 챌린지 | 챌린지 생성, 참여, 진행률 수정, 탈퇴, 삭제 |
| 소셜 | 팔로우/언팔로우, 팔로잉/팔로워, 추천 사용자, 리더보드 |
| REST API | 인증, 사용자, 홈/코치 대시보드, 식단, 음식 검색, 커뮤니티, 챌린지, 소셜, 헬스 체크, 영양 DB 배치 실행 API |
| 배치 | CSV/XLSX 영양성분 파일 staging, 정규화, `food_nutrition` upsert, 리포트 생성 |
| 공통 | 로그인 필터, 예외 처리, AOP 로깅, Swagger UI |

## 2. 실행 구조

프로젝트는 Spring Boot 3.5.x, Java 21, Maven 기반이다.

주요 의존성은 다음과 같다.

- `spring-boot-starter-web`: Spring MVC와 내장 Tomcat
- `spring-boot-starter-security`: BCrypt 비밀번호 암호화와 보안 기반 설정
- `spring-boot-starter-aop`: 서비스/리포지토리 실행 로깅
- `spring-boot-starter-batch`: 영양성분 DB 적재 배치
- `spring-boot-starter-jdbc`: JDBC 기반 DB 접근
- `mysql-connector-j`: MySQL 연결
- `tomcat-embed-jasper`, JSTL: JSP 화면 렌더링
- `springdoc-openapi`: Swagger UI와 OpenAPI 문서
- `poi-ooxml`: XLSX 영양성분 파일 읽기

기존 화면은 `/WEB-INF/views/**/*.jsp`에 있고, 컨트롤러는 문자열 view name을 반환한다. Vue SPA 화면은 `client/src/views/**/*.vue`에 있으며, `client/src/composables`의 Axios 기반 API composable로 Spring REST API를 호출한다.

## 3. 인증과 접근 제어

### 3.1 세션 기반 인증

로그인은 `AuthController`와 `AuthApiController`가 처리한다.

사용자는 이메일과 비밀번호로 로그인하며, 성공 시 세션에 `loginUserId`가 저장된다. 이후 요청에서는 `SessionUtils.currentUserId(...)`로 현재 로그인 사용자를 확인한다.

회원가입 시 입력받는 주요 정보는 다음과 같다.

- email
- password
- nickname
- gender
- birthYear
- height
- weight
- goal
- healthNote

비밀번호는 `BCryptPasswordEncoder`로 암호화되어 저장된다. 기존 평문 비밀번호가 남아 있는 경우 로그인 성공 시 BCrypt 해시로 자동 마이그레이션하는 로직도 들어 있다.

### 3.2 로그인 필터

실제 접근 제어의 중심은 `LoginCheckFilter`다.

이 필터는 요청마다 다음을 수행한다.

- flash 메시지를 request attribute로 노출
- 세션의 `loginUserId`로 현재 사용자 조회
- 비활성 사용자면 세션 로그아웃 처리
- 공개 경로가 아니고 로그인 사용자가 없으면 차단
- API 요청이면 `401` JSON 응답
- 일반 화면 요청이면 로그인 페이지로 redirect
- 로그인 사용자를 `currentUser` request attribute에 저장

Spring Security 설정은 CSRF, form login, http basic을 비활성화하고 대부분 요청을 permitAll로 둔다. 따라서 현재 보안 흐름은 Spring Security 인증 체계보다 자체 세션 필터에 더 크게 의존한다.

## 4. 데이터 저장 방식

현재 프로젝트는 저장소가 혼합되어 있다.

| 영역 | 저장 방식 |
| --- | --- |
| 사용자 | MySQL JDBC |
| 식단/식단 음식 | MySQL JDBC |
| 음식 영양 카탈로그 | MySQL JDBC |
| 커뮤니티 게시글/댓글 | MySQL JDBC |
| 영양성분 배치 staging/report | MySQL JDBC (`JdbcTemplate`) |
| Spring Batch 메타데이터 | MySQL, Spring Batch JDBC 스키마 |
| 챌린지 | 메모리 `Map` |
| 챌린지 참여 | 메모리 `Map` |
| 팔로우 관계 | 메모리 `Map` |

일반 도메인 리포지토리의 다수 구현은 Spring `DataSource` 대신 `DBUtil`의 `DriverManager.getConnection(...)`을 직접 사용한다. DB 접속 정보는 `application.properties`와 `DBUtil`에 모두 존재하므로 불일치 위험이 있다. 반면 영양성분 배치 리포지토리는 Spring Boot가 구성한 `JdbcTemplate`을 사용한다.

사용자, 식단, 커뮤니티 리포지토리는 시작 시 `CREATE TABLE IF NOT EXISTS`로 필요한 테이블을 생성하고, 비어 있으면 `SeedDataFactory`의 초기 데이터를 저장한다.

영양성분 배치 리포지토리는 시작 시 `nutrition_import_staging`, `nutrition_import_report` 테이블을 생성한다. Spring Batch 메타 테이블은 `spring.batch.jdbc.initialize-schema=always` 설정에 따라 초기화된다.

챌린지와 소셜 팔로우는 DB가 아니라 애플리케이션 메모리에 저장되므로 서버 재시작 시 런타임 변경 사항은 유지되지 않는다.

## 5. 식단 관리 기능

식단 기능은 프로젝트의 중심 기능이다.

### 5.1 화면 기능

`MealController`는 `/meals` 아래에서 다음 화면 기능을 제공한다.

| 경로 | 기능 |
| --- | --- |
| `GET /meals` | 내 식단 목록 조회, 날짜/식사 타입 필터, 정렬 |
| `GET /meals/detail?mealId=...` | 식단 상세와 분석 결과 조회 |
| `GET /meals/new` | 식단 등록 화면 |
| `GET /meals/edit?mealId=...` | 식단 수정 화면 |
| `POST /meals/new` | 식단 생성 |
| `POST /meals/edit` | 식단 수정 |
| `POST /meals/delete` | 식단 삭제 |

목록 정렬은 다음 기준을 지원한다.

- 최신순 `dateDesc`
- 오래된순 `dateAsc`
- 칼로리 높은순 `energyDesc`
- 분석 점수 높은순 `scoreDesc`

식단 등록/수정 시 사용자는 음식 코드를 선택하고, 각 음식의 섭취량을 `grams_{foodCode}` 형태로 전달한다. 서버는 음식 카탈로그에서 기본 영양 정보를 찾고, 섭취량에 맞춘 `FoodItem`을 식단에 저장한다.

### 5.2 영양 계산

`MealService`는 선택된 음식 목록을 합산해 다음 영양 요약을 계산한다.

- calories
- carbs
- protein
- fat
- 탄수화물/단백질/지방 열량 비율

사용자의 일일 목표는 성별, 나이, 키, 몸무게, 목표를 기반으로 계산한다.

- 여성: `655.1 + 9.56*weight + 1.85*height - 4.68*age`
- 남성/기본: `66.47 + 13.75*weight + 5.0*height - 6.76*age`
- 활동 계수: `1.45`
- 목표가 `diet`면 350kcal 감산
- 목표가 `muscle`이면 250kcal 가산
- 탄수화물/단백질/지방 비율은 50/25/25 기준

식사별 목표 칼로리는 일일 목표에서 다음 비율로 나눈다.

| 식사 | 비율 |
| --- | --- |
| breakfast | 28% |
| lunch | 34% |
| dinner | 28% |
| snack | 10% |

### 5.3 식단 분석

식단 상세에서는 `MealService.analyzeMeal(...)`로 점수와 등급을 만든다.

분석 기준은 다음과 같다.

- 식사 목표 칼로리와 실제 칼로리 차이
- 단백질 비율 부족 여부
- 지방 비율 과다 여부
- 탄수화물 비율 과다 여부

결과는 다음 형태로 사용된다.

- 점수 `45~100`
- 등급 `A/B/C/D`
- 식단 인사이트 문장
- 다음 행동 제안
- 영양 요약

이 분석은 외부 AI 모델 호출이 아니라, 규칙 기반 계산 로직이다.

### 5.4 음식 검색과 추천

음식 카탈로그는 `food_nutrition` 테이블에서 읽는다.

`FoodCatalogRepository`는 다음을 수행한다.

- 전체 음식 조회
- 음식 코드로 단건 조회
- 음식명 또는 카테고리 키워드 검색
- 원본 제공 중량 문자열에서 숫자를 추출
- 100g 기준으로 energy/carbs/protein/fat 정규화

식단 입력 화면의 추천 음식은 현재 선택된 음식의 칼로리와 식사 목표 칼로리의 차이를 기준으로, 남은 칼로리에 가까운 음식을 골라 보여준다.

## 6. 영양성분 DB 배치 기능

현재 프로젝트에는 외부 영양성분 파일을 음식 카탈로그(`food_nutrition`)로 적재하는 Spring Batch 작업이 구현되어 있다.

### 6.1 배치 실행 API

`NutritionBatchApiController`는 `/batch` 아래에서 수동 실행 API를 제공한다.

| 경로 | 기능 |
| --- | --- |
| `GET /batch/nutrition-import?sourcePath=...` | CSV/XLSX 영양성분 파일 적재 시작 |
| `GET /batch/nutrition-import/restart?executionId=...` | 실패 또는 중단된 배치 실행 재시작 |

배치 시작 API는 다음 파라미터를 받는다.

- `sourcePath`: 서버 로컬 기준 원본 파일 경로
- `sourceName`: 리포트에 표시할 원본 이름, 생략 시 파일명 사용
- `chunkSize`: 1~500 사이로 보정되는 청크 크기, 기본값 100
- `runId`: JobInstance 식별용 실행 ID, 생략 시 현재 시간 사용

응답은 `202 Accepted`와 함께 `executionId`, 초기 `status`, `sourceName`, `pdfReportPath`를 반환한다. 원본 파일이 없으면 `400 Bad Request`를 반환한다.

### 6.2 배치 Job 구조

`nutritionImportJob`은 네 단계로 구성된다.

| Step | 역할 |
| --- | --- |
| `nutritionStageStep` | CSV/XLSX 원본 행을 `nutrition_import_staging`에 저장 |
| `nutritionNormalizeUpsertStep` | staging 행을 읽어 필수값 검증, 숫자 정규화, `food_nutrition` upsert 수행 |
| `nutritionImportReportStep` | 실행별 성공/실패/대기 건수를 `nutrition_import_report`에 저장 |
| `nutritionImportPdfReportStep` | `reports/batch/nutrition/nutrition-import-{executionId}.pdf` 리포트 생성 |

CSV는 UTF-8 BOM/UTF-8/MS949 인코딩을 감지하고, XLSX는 Apache POI로 읽는다. 파일 확장자는 `.csv`, `.xlsx`만 지원한다.

### 6.3 정규화와 실패 처리

`NutritionSourceProfile`은 다양한 원본 컬럼명을 내부 `TargetField`로 매핑한다. 예를 들어 `식품명`, `음식명`, `food_name`은 음식명으로, `에너지(kcal)`, `열량`, `칼로리`는 칼로리로 처리한다.

`NutritionNormalizeProcessor`는 다음 규칙을 적용한다.

- 음식명과 칼로리는 필수값이다.
- 숫자 필드는 쉼표, 부등호, `약`, 단위 문자열 등을 제거한 뒤 `BigDecimal`로 변환한다.
- 음식 코드가 없으면 `sourceName`, 음식명, 카테고리, 중량을 기반으로 SHA-256 해시 코드(`SRC_...`)를 생성한다.
- 음식 코드가 50자를 넘으면 실패 처리한다.
- 정규화 성공 행은 `DONE`, 실패 행은 `FAILED`로 staging 상태를 갱신한다.

PDF 리포트에는 실행 ID, 원본 이름/경로, 시작/종료 시각, 전체/성공/실패/대기 건수, 실패 샘플 최대 20건이 기록된다. 현재 PDF 작성기는 내장 단순 writer이며 한글 등 비 ASCII 문자는 `?`로 치환된다.

## 7. AI 코치 기능

프로젝트에서 “AI 코치”라고 부르는 기능은 현재 규칙 기반 코칭 대시보드다.

`CoachController`는 두 가지를 제공한다.

| 경로 | 기능 |
| --- | --- |
| `GET /coach` | 코치 JSP 화면 |
| `GET /coach/dashboard` | 코치 대시보드 JSON |

`CoachService`는 다음 데이터를 조합한다.

- 최근 식단 3건
- 최근 식단별 분석 결과
- 오늘 섭취 영양 요약
- 사용자 일일 목표
- 목표 대비 오늘 칼로리 달성률
- 사용자 목표에 따른 운동 세션 추천
- 단백질/칼로리/탄수화물 상태 기반 다음 행동
- 참여 중인 챌린지 기반 행동 제안

따라서 현재 코치 기능은 LLM이나 외부 AI를 호출하지 않고, 식단/목표/챌린지 데이터를 바탕으로 미리 정의된 규칙을 적용해 조언을 생성한다.

## 8. 홈과 프로필

### 8.1 홈 대시보드

`HomeController`의 `/`와 `/home`은 로그인 사용자의 요약 대시보드를 구성한다.

표시 데이터는 다음과 같다.

- 최근 식단 3건
- 오늘 섭취 영양 요약
- 일일 목표 영양
- 코치 조언
- 활성 챌린지 일부
- 팔로잉/팔로워 수

### 8.2 프로필

`ProfileController`의 `/profile`은 사용자 프로필과 활동 통계를 보여준다.

프로필 화면은 다음을 제공한다.

- 현재 사용자 정보 조회
- 이메일, 닉네임, 비밀번호, 성별, 출생연도, 키, 몸무게, 목표, 건강 메모 수정
- 식단 수, 팔로잉 수, 팔로워 수, 참여 챌린지 수 표시
- 계정 비활성화

현재 화면의 계정 삭제/비활성화 액션은 실제 물리 삭제가 아니라 `active=false` 처리 후 로그아웃하는 방식이다.

## 9. 커뮤니티 기능

`CommunityController`와 `CommunityService`는 게시글과 댓글 기능을 제공한다.

기능 범위는 다음과 같다.

- 게시글 목록 조회
- 카테고리 필터: `review`, `expert`, `free`, `all`
- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 댓글 작성
- 댓글 수정
- 댓글 삭제
- 게시글에 내 식단 기록 연결
- 게시글/댓글 작성자 닉네임 표시

게시글과 댓글은 MySQL에 저장된다. 댓글은 `community_comments.post_id` 외래키로 게시글에 연결되며, 게시글 삭제 시 댓글도 삭제되도록 `ON DELETE CASCADE`가 설정되어 있다.

수정 화면은 별도 JSP가 아니라 `community/index.jsp`에 `editPost` 또는 `editComment` 모델을 넣어 같은 화면에서 수정 상태를 렌더링한다.

## 10. 챌린지 기능

`ChallengeController`와 `ChallengeService`는 건강 활동 챌린지 기능을 제공한다.

기능 범위는 다음과 같다.

- 챌린지 목록 조회
- 챌린지 생성
- 챌린지 참여
- 내 진행률 수정
- 목표 달성 시 상태를 `completed`로 변경
- 챌린지 탈퇴
- 생성자 본인의 챌린지 삭제
- 참여자 목록과 진행률 표시
- 참여/완료/생성 개수 요약

챌린지는 제목, 설명, 카테고리, 목표 횟수, 시작일, 종료일, 생성자 정보를 가진다. 진행률은 `0` 이상 `targetCount` 이하로 보정된다.

중요한 한계는 챌린지 데이터가 DB가 아니라 메모리 저장소에 있다는 점이다. 서버가 재시작되면 새로 생성한 챌린지와 진행률 변경은 유지되지 않는다.

## 11. 소셜 기능

`SocialController`와 `SocialService`는 사용자 간 팔로우 기능을 제공한다.

기능 범위는 다음과 같다.

- 내가 팔로우한 사용자 목록
- 나를 팔로우한 사용자 목록
- 추천 사용자 목록
- 팔로워 수 기반 리더보드
- 팔로우
- 언팔로우
- 팔로워/팔로잉 수 계산

추천 사용자는 활성 사용자 중 본인과 이미 팔로우한 사용자를 제외하고, 팔로워 수가 많은 순으로 정렬해 보여준다. 리더보드도 팔로워 수 기준이다.

현재 팔로우 관계도 메모리 저장소에 저장된다. 따라서 운영 관점에서는 영속화가 필요한 영역이다.

## 12. REST API 기능

프로젝트는 JSP 화면과 별도로 일부 REST API를 제공한다.

| API | 기능 |
| --- | --- |
| `POST /api/v1/auth/login` | 로그인 후 세션 생성 |
| `POST /api/v1/auth/signup` | 회원가입 후 세션 생성 |
| `POST /api/v1/auth/logout` | 세션 종료 |
| `GET /api/v1/users/me` | 내 프로필 조회 |
| `PUT /api/v1/users/me` | 내 프로필 수정 |
| `GET /api/v1/meals` | 내 식단 목록 조회 |
| `GET /api/v1/meals/{mealId}` | 식단 상세 조회 |
| `POST /api/v1/meals` | 식단 생성 |
| `PUT /api/v1/meals/{mealId}` | 식단 수정 |
| `DELETE /api/v1/meals/{mealId}` | 식단 삭제 |
| `GET /api/v1/foods?keyword=...` | 음식 검색 |
| `GET /api/v1/dashboard/home` | 홈 대시보드 조회 |
| `GET /api/v1/coach/dashboard` | 코치 대시보드 조회 |
| `GET /api/v1/community` | 커뮤니티 게시글/댓글 목록 조회 |
| `POST/PATCH/DELETE /api/v1/community/...` | 커뮤니티 게시글/댓글 변경 |
| `GET/POST/PATCH/DELETE /api/v1/challenges...` | 챌린지 조회/생성/참여/진행률/탈퇴/삭제 |
| `GET/POST/DELETE /api/v1/social...` | 소셜 조회/팔로우/언팔로우 |
| `GET /api/v1/health` | API 상태 확인 |
| `GET /batch/nutrition-import` | 영양성분 DB 배치 적재 시작 |
| `GET /batch/nutrition-import/restart` | 영양성분 DB 배치 재시작 |

REST API도 세션 기반 인증을 사용한다. 인증이 필요한 API에서 로그인 세션이 없으면 `LoginCheckFilter`가 `401` JSON을 반환한다. `/batch/**` 경로는 Swagger 문서에는 포함되지만 `/api/v1` 하위가 아닌 별도 경로다.

REST 예외는 `ApiExceptionHandler`가 `ProblemDetail` 형식으로 처리한다. JSP 화면 예외는 `GlobalExceptionHandler`가 `error/error.jsp`를 반환한다.

Swagger UI는 `/swagger-ui/index.html`에서 확인할 수 있도록 설정되어 있다. OpenAPI 문서 대상 경로는 `/api/v1/**`, `/batch/**`다.

## 13. 공통 구현 요소

### 13.1 AOP 로깅

`LoggingAspect`는 서비스와 리포지토리 메서드 실행을 감싼다.

로그에 남기는 정보는 다음과 같다.

- 진입 메서드 시그니처
- 인자 타입
- 정상 종료 시 소요 시간
- 예외 발생 시 소요 시간과 스택 트레이스

### 13.2 정렬 유틸리티

프로젝트는 Java 표준 정렬만 쓰지 않고 `SortUtils` 기반 정렬을 일부 사용한다.

사용 예는 다음과 같다.

- 식단 목록 quick sort
- 게시글/댓글 정렬 quick sort
- 챌린지/참여자 정렬 quick sort
- 음식 추천 후보 counting sort
- 식단 내부 음식 selection sort

### 13.3 ViewHelper와 SessionUtils

`ViewHelper`는 JSP에서 사용할 날짜/라벨/널 처리 형식화를 담당한다. `SessionUtils`는 로그인 세션과 flash 메시지 저장/노출을 담당한다.

## 14. 현재 프로젝트의 성격

현재 프로젝트는 “식단 기록 기반 건강 코칭 웹 서비스”라고 볼 수 있다.

사용자 관점의 핵심 흐름은 다음과 같다.

1. 회원가입 또는 로그인한다.
2. 음식 카탈로그에서 음식을 검색해 식단을 기록한다.
3. 식단별 칼로리와 탄단지 비율을 확인한다.
4. 목표 대비 식단 점수와 개선 조언을 본다.
5. 홈/코치 화면에서 오늘의 요약과 다음 행동을 확인한다.
6. 커뮤니티에 식단 경험을 공유하거나 댓글을 단다.
7. 챌린지에 참여해 진행률을 기록한다.
8. 다른 사용자를 팔로우하고 리더보드를 본다.

관리/운영 관점에서는 CSV/XLSX 영양성분 파일을 배치로 적재해 음식 카탈로그를 확장할 수 있다.

기술적으로는 Servlet/JSP 프로젝트를 Spring MVC 계층 구조로 정리하고, 일부 REST API, Swagger 문서, Spring Batch 기반 영양 데이터 적재 기능을 추가한 상태다. 다만 전체가 JPA나 MyBatis로 통합된 구조는 아니며, 직접 JDBC와 `JdbcTemplate`, 일부 메모리 저장소가 혼재한다.

## 15. 구현상 주의할 점

현재 구현을 바탕으로 추가 개발할 때 주의할 점은 다음과 같다.

- 일반 도메인 리포지토리의 `DBUtil`에 DB 접속 정보가 하드코딩되어 있어 `application.properties`와 불일치할 수 있다.
- 챌린지와 소셜 데이터는 메모리 저장소이므로 재시작 후 변경 사항이 사라진다.
- Spring Security의 인증/인가 기능은 적극적으로 사용하지 않고, 자체 필터가 접근 제어를 담당한다.
- Vue SPA는 Vite 개발 서버에서 실행하고 `/api`, `/batch`, `/swagger-ui`, `/v3/api-docs` 요청을 Spring Boot 서버로 proxy한다.
- 영양성분 배치 API는 서버 로컬 파일 경로를 입력받으므로 운영 환경에서는 호출 권한과 접근 가능한 경로 제한이 필요하다.
- 배치 PDF 리포트는 단순 ASCII 기반 writer라 한글 파일명/음식명은 PDF 안에서 `?`로 보일 수 있다.
- 코치 기능은 실제 AI 연동이 아니라 규칙 기반 추천이다.
- 계정 삭제 화면 흐름은 현재 물리 삭제가 아니라 비활성화 중심으로 동작한다.
- Repository가 SQL과 스키마 생성을 직접 포함하고 있어 규모가 커질수록 마이그레이션 관리가 필요하다.
- 사용자 입력 검증은 서비스 단에서 기본적인 수준으로 처리되며, Bean Validation은 사용하지 않는다.

## 16. 결론

YumYumCoach는 식단 기록과 영양 분석을 중심으로, 코칭/커뮤니티/챌린지/소셜 요소를 결합한 건강 관리 웹 애플리케이션이다.

현재 완성도가 높은 영역은 세션 인증, 사용자/식단/음식 검색, 영양 계산, 식단 분석, 커뮤니티 CRUD, JSP 화면 흐름, Vue SPA 화면 흐름, REST API, 영양성분 DB 배치 적재다. 반면 챌린지와 소셜은 기능 화면과 API는 갖추었지만 저장소가 메모리 기반이라 운영 영속성은 부족하다.

따라서 현재 프로젝트는 “Spring MVC/JSP 기반 건강 코칭 서비스에 Vue SPA를 병행 전환한 구현체”이며, 다음 단계에서는 챌린지/소셜 DB 영속화, JSP 라우트 제거 또는 완전 분리 배포, 보안 설정 정교화, 배치 운영 권한 관리, 실제 AI 또는 규칙 엔진 분리 등을 붙이기 좋은 상태다.
