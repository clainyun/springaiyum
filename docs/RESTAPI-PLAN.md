# REST API 전환 실행 계획

## 1. 목표 재정의

이 프로젝트의 목표는 "최신식 대규모 백엔드 아키텍처"를 만드는 것이 아니다.  
현재 교육 진도와 참고 프로젝트 `FW_10_rest_lab`를 기준으로 했을 때, 가장 적절한 목표는 다음과 같다.

- 기존 JSP 기반 화면은 당분간 유지
- `/api/v1/**` 하위에 `@RestController` 기반 API를 병행 추가
- Swagger(OpenAPI)로 REST API를 문서화
- AOP로 공통 로깅을 정리
- Spring Security는 **PasswordEncoder + 기본 접근 제어 + 세션 기반 인증 유지** 수준까지 도입
- JWT, OAuth2, 복잡한 인증 아키텍처는 이번 범위에서 제외

즉, 이 문서는 "REST API 서버로의 점진적 전환" 계획이면서도, 실제 작업 수준은 **교육용 프로젝트에 맞는 단순한 Spring Boot + REST + Swagger + Security 기초 적용**에 맞춘다.

## 2. 이번 계획의 기준선

## 2.1 참고 프로젝트에서 확인된 교육 범위

`FW_10_rest_lab`를 분석한 결과, 현재 교육 범위의 중심은 아래와 같다.

- Spring MVC + REST Controller 병행
- 세션 기반 로그인
- Interceptor 기반 인증 체크
- AOP 기반 로깅
- Swagger 문서화
- `ResponseEntity` 기반 REST 응답
- `ProblemDetail` 기반 REST 예외 처리
- `WebMvcTest` + Mockito 기반 컨트롤러 테스트

중요한 점은 참고 프로젝트에 **Spring Security가 직접 들어가 있지 않다**는 것이다.

- `spring-boot-starter-security` 없음
- `SecurityFilterChain` 없음
- `PasswordEncoder` 없음
- `UserDetailsService` 없음
- JWT/OAuth2 없음

따라서 지금 레포에 Security를 넣더라도, 상한은 아래 정도가 적절하다.

- `PasswordEncoder` 빈 등록
- `SecurityFilterChain` 1개 구성
- 공개/보호 URL 단순 관리
- 세션 기반 인증 유지

## 2.2 이번 프로젝트에서 반드시 반영할 보안 개선

이번 작업 단위에서는 `PasswordEncoder` 도입을 필수로 본다.

즉, 적어도 아래는 반드시 구현 대상이다.

- `spring-boot-starter-security` 추가
- `PasswordEncoder` 빈 등록
- 회원가입 시 비밀번호 인코딩
- 로그인 시 `matches()` 비교
- 비밀번호 변경 시 재인코딩
- Demo/Seed 데이터 비밀번호도 해시값으로 정리

## 3. 기술 상한과 제외 범위

## 3.1 이번 범위에 포함하는 것

- `@RestController`
- Swagger
- `@RestControllerAdvice`
- `ProblemDetail` 또는 단순 `ErrorResponse`
- `spring-boot-starter-aop`
- `spring-boot-starter-security`
- `SecurityFilterChain`
- `PasswordEncoder`
- 세션 기반 인증 유지
- `WebMvcTest` 기반 API 테스트

## 3.2 이번 범위에서 제외하는 것

- JWT Access/Refresh Token
- OAuth2 로그인
- Redis
- `UserDetailsService`/`AuthenticationProvider` 과도한 커스터마이징
- `@PreAuthorize` 남발
- role hierarchy
- 복잡한 공통 응답 래퍼 강제
- Password 인코딩을 AOP로 숨기는 방식

## 4. 설계 원칙

## 4.1 MVC와 REST를 병행한다

- 기존 `@Controller` + JSP는 유지한다.
- 새 기능과 점진 전환은 `controller.api` 패키지의 `@RestController`에서 담당한다.
- 기존 화면을 한 번에 제거하지 않는다.

## 4.2 인증 책임은 하나로 정리한다

현재 레포에는 이미 아래 구조가 있다.

- `AuthService`
- `SessionUtils`
- `LoginCheckFilter`

여기에 Spring Security를 추가할 때 가장 중요한 원칙은 다음이다.

**URL 접근 제어 책임을 `LoginCheckFilter`와 Security가 동시에 강하게 나눠 갖지 않도록 한다.**

이번 계획에서는 점진적으로 아래 방향을 취한다.

- URL 보호 규칙은 `SecurityFilterChain`으로 옮긴다.
- 기존 `LoginCheckFilter`는 축소하거나 제거 대상으로 본다.
- 로그인 자체는 당장 JWT로 바꾸지 않고 세션 기반으로 유지한다.

## 4.3 비밀번호 로직은 서비스 계층에 둔다

비밀번호 인코딩은 `aspect`가 아니라 서비스 로직에서 명시적으로 처리한다.

적용 위치:

- `AuthService.register()`
- `AuthService.login()`
- `UserService.updateProfile()` 또는 프로필 수정 흐름

적용하지 않을 위치:

- AOP
- Repository
- Controller 직접 처리

## 4.4 문서화는 Swagger를 공식 기준으로 삼는다

- REST API는 Swagger에 문서화한다.
- `docs/API.md`는 보조 메모 문서로 둔다.
- JSP 컨트롤러는 Swagger 문서 대상에서 제외한다.

## 5. 목표 구조

```text
src/main/java/com/ssafy/yumyum
├── aspect
│   └── LoggingAspect.java
├── config
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller
│   └── ... (기존 JSP 컨트롤러 유지)
├── controller/api
│   ├── AuthApiController.java
│   ├── UserApiController.java
│   ├── MealApiController.java
│   ├── CommunityApiController.java
│   ├── ChallengeApiController.java
│   └── SocialApiController.java
├── dto
│   ├── auth
│   ├── user
│   ├── meal
│   ├── community
│   ├── challenge
│   └── social
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ApiExceptionHandler.java
├── repository
├── service
└── util
```

## 6. 우선 구현 순서

이전 계획보다 더 현실적으로, 작업 순서를 다음처럼 재정렬한다.

## 6.1 1단계: 기반 정리

가장 먼저 해야 할 것은 "REST API를 추가할 수 있는 기반"과 "비밀번호 보안 최소 수준"을 만드는 일이다.

### 작업 항목

- `pom.xml` 업데이트
  - `spring-boot-starter-security`
  - `spring-boot-starter-aop`
  - `springdoc-openapi-starter-webmvc-ui`
- `config/SecurityConfig.java` 추가
- `PasswordEncoder` 빈 등록
- Swagger 설정 추가
- `aspect/LoggingAspect.java` 추가
- `exception/ApiExceptionHandler.java` 추가

### SecurityConfig 목표

- `/auth/**` 공개
- Swagger 경로 공개
- 정적 리소스 공개
- `/api/v1/**` 포함 나머지 보호
- 기본 로그인 페이지는 사용하지 않음
- 세션 기반 인증 유지

### 이 단계의 완료 조건

- 프로젝트가 Security 의존성을 가진 상태로 정상 기동 가능
- PasswordEncoder 빈이 사용 가능
- Swagger UI 접속 가능
- REST 예외 응답 틀이 있음

## 6.2 2단계: 인증 흐름 보안화

현재 프로젝트는 평문 비밀번호를 사용하므로, REST 전환보다 먼저 이 부분을 바로잡는다.

### 작업 항목

- `AuthService.login()`에서 `passwordEncoder.matches()` 사용
- `AuthService.register()`에서 `passwordEncoder.encode()` 사용
- 프로필 수정 시 비밀번호 변경 로직에 `encode()` 반영
- seed/demo 비밀번호 해시 전략 정리
- 필요하면 초기 데이터 재생성 정책 정리

### 설계 원칙

- 로그인 성공 후에는 기존처럼 세션을 사용한다.
- 단, Security와 세션 흐름이 충돌하지 않도록 인증 상태 관리 방식을 정리한다.

### 이 단계의 완료 조건

- 평문 비밀번호 비교 제거
- 새 사용자 등록 시 해시 저장
- 로그인/비밀번호 변경 흐름이 모두 인코딩 정책을 따름

## 6.3 3단계: REST 골격 API 추가

보안 기반이 갖춰진 뒤, 가장 단순한 REST API부터 추가한다.

### 우선 대상

- 인증 API
- 내 정보 API

### 추천 엔드포인트

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

### 응답 스타일

참고 프로젝트 수준에 맞춰 단순하게 간다.

- `ResponseEntity`
- DTO 또는 `Map.of(...)`
- 오류는 `ProblemDetail` 또는 단순 `ErrorResponse`

### 이 단계의 완료 조건

- Swagger에서 인증/내 정보 API 확인 가능
- 세션 로그인 기반으로 API 호출 가능
- 미인증 접근 시 일관된 401 응답 확인 가능

## 6.4 4단계: 식단/음식 API 추가

이 프로젝트의 핵심 기능이며 JDBC가 이미 있으므로 가장 먼저 REST화할 가치가 높다.

### 추천 엔드포인트

- `GET /api/v1/meals`
- `GET /api/v1/meals/{mealId}`
- `POST /api/v1/meals`
- `PUT /api/v1/meals/{mealId}`
- `DELETE /api/v1/meals/{mealId}`
- `GET /api/v1/foods?keyword=...`

### 구현 원칙

- `MealController`의 수동 파라미터 파싱을 DTO 중심으로 대체
- 컨트롤러는 입력/응답에 집중
- 비즈니스 검증은 서비스에 유지

### 이 단계의 완료 조건

- 식단 CRUD가 Swagger에서 확인 가능
- 음식 검색 API 동작
- 기존 JSP 화면과 병행 가능

## 6.5 5단계: 커뮤니티 API 추가

커뮤니티는 이미 JDBC 기반이므로 다음 순서로 전환한다.

### 추천 엔드포인트

- `GET /api/v1/community/posts`
- `GET /api/v1/community/posts/{postId}`
- `POST /api/v1/community/posts`
- `PUT /api/v1/community/posts/{postId}`
- `DELETE /api/v1/community/posts/{postId}`
- `GET /api/v1/community/posts/{postId}/comments`
- `POST /api/v1/community/posts/{postId}/comments`
- `PUT /api/v1/community/comments/{commentId}`
- `DELETE /api/v1/community/comments/{commentId}`

### 이 단계의 완료 조건

- 게시글/댓글 CRUD가 REST로 제공됨
- Swagger 문서 반영
- 기존 JSP 커뮤니티와 병행 운용 가능

## 6.6 6단계: DB 접근 구조 정리

이 단계는 기능 추가와 병행 가능하지만, 늦어도 핵심 REST API가 자리 잡기 전에는 정리해야 한다.

현재는:

- `application.properties`에 datasource 설정 존재
- 실제 코드에서는 `DBUtil` 하드코딩 연결 사용

이중 구조는 제거해야 한다.

### 목표

- Spring `DataSource` 중심으로 정리
- `DBUtil` 제거 또는 최소화
- Repository 연결 방식 일원화

### 권장 수준

교육 범위를 고려하면 `JdbcTemplate`까지는 가능하지만, 무리해서 전 리포지토리를 크게 재작성할 필요는 없다.  
핵심은 "Spring 설정을 쓰는 방향으로 연결 방식을 통일"하는 것이다.

## 6.7 7단계: 스키마 불일치 해소

현재는 코드와 SQL 문서가 다르다.

대표 불일치:

- `users.user_id` 타입
- `meals/meal_foods` vs `diet_logs/diet_log_items`
- 커뮤니티 테이블 문서 누락

REST API 서버를 안정적으로 운영하려면 스키마 기준을 하나로 통일해야 한다.

### 이 단계의 목표

- 공식 스키마 하나로 정리
- README, SQL, Repository 구현 정합성 확보
- seed/demo 데이터 정책도 함께 정리

## 6.8 8단계: 챌린지/소셜 영속화 후 REST 공개

현재 `ChallengeRepository`, `SocialRepository`는 메모리 기반이다.

즉, 지금 상태에서 REST API로 공개해도 재시작 시 데이터가 사라진다.

### 먼저 해야 할 것

- `challenges`
- `challenge_memberships`
- `follow_relations`

위 테이블을 설계하고 JDBC 기반 저장소로 바꾼다.

### 그다음 추가할 API

- `GET /api/v1/challenges`
- `POST /api/v1/challenges`
- `POST /api/v1/challenges/{challengeId}/memberships`
- `PATCH /api/v1/challenges/{challengeId}/memberships/me`
- `DELETE /api/v1/challenges/{challengeId}/memberships/me`
- `GET /api/v1/users/{userId}/followers`
- `GET /api/v1/users/{userId}/following`
- `POST /api/v1/users/{userId}/follow`
- `DELETE /api/v1/users/{userId}/follow`

## 6.9 9단계: 코치/대시보드 API 정리

현재 `GET /coach/dashboard`는 이미 JSON을 반환한다.

이건 정식 REST API 구조로 흡수하기 좋다.

### 목표

- `/api/v1/coach/dashboard`로 이관 또는 병행 제공
- 홈 대시보드도 필요 시 API화

이 단계는 앞선 핵심 CRUD API가 정리된 뒤 진행하면 충분하다.

## 7. 구현 스타일 가이드

## 7.1 REST 응답

이번 수준에서는 아래 정도가 적절하다.

- 성공: DTO 반환 또는 `Map.of(...)`
- 실패: `ProblemDetail` 또는 단순 `ErrorResponse`

지금 단계에서 전역 `ApiResponse<T>` 강제는 필요하지 않다.

## 7.2 Controller 책임

컨트롤러는 최대한 얇게 유지한다.

- 입력 받기
- 서비스 호출
- 응답 반환

지양할 것:

- `HttpServletRequest` 직접 파라미터 파싱 남발
- 컨트롤러 내부 비즈니스 검증 과다

## 7.3 Aspect 책임

`aspect` 패키지는 아래 용도로만 쓴다.

- 요청/서비스/리포지토리 로깅
- 실행 시간 측정
- 예외 로깅

하지 않을 것:

- 비밀번호 인코딩
- 보안 핵심 로직

## 7.4 Security 책임

Security는 이번 범위에서 아래까지만 맡긴다.

- URL 보호
- 공개/인증 필요 경로 분리
- 세션 기반 인증 상태 활용
- PasswordEncoder 제공

하지 않을 것:

- JWT 발급
- OAuth2 로그인
- 복잡한 권한 모델링

## 8. 테스트 전략

참고 프로젝트와 같은 수준으로 간다.

### 권장 방식

- `@WebMvcTest`
- Mockito 기반 서비스 목 처리
- 상태 코드 검증
- JSON 필드 검증

### 우선 테스트 대상

1. Auth API
2. User API
3. Meal API
4. Community API
5. Security 미인증 접근

### 최소 체크 항목

- 로그인 성공/실패
- 회원가입 성공
- 비밀번호 인코딩 적용 여부
- 미인증 시 401 또는 적절한 리다이렉트/JSON

## 9. 1차 목표와 2차 목표

## 9.1 1차 목표

"수업 범위 안에서 설명 가능한 REST + Swagger + Security 기초 구조를 갖춘 상태"

포함 범위:

- Security 의존성 추가
- PasswordEncoder 빈 등록
- SecurityFilterChain 설정
- Swagger 설정
- LoggingAspect 추가
- Auth/User REST API
- Meal/Food REST API
- Community REST API
- REST 예외 처리

## 9.2 2차 목표

"메모리 저장소까지 제거하고, 주요 도메인을 REST 중심 구조로 정리한 상태"

포함 범위:

- DB 접근 구조 정리
- 스키마 통일
- Challenge JDBC 포팅
- Social JDBC 포팅
- Challenge/Social REST API
- Coach API 정리

## 10. 최종 방향

이 프로젝트는 참고 프로젝트보다 약간 더 발전된 형태로 가되, 교육 범위를 크게 벗어나지 않아야 한다.

따라서 가장 적절한 방향은 다음과 같다.

- REST API는 `@RestController`로 점진 추가
- Swagger는 공식 API 문서로 사용
- 로깅은 `aspect` 패키지에서 처리
- Spring Security는 PasswordEncoder와 기본 접근 제어 수준까지 적극 활용
- 인증은 당분간 세션 기반 유지
- JWT/OAuth2 같은 확장형 설계는 이번 범위에서 제외

한 문장으로 정리하면 이렇다.

**"기존 MVC 자산을 유지하면서, REST + Swagger + AOP + Spring Security 기초를 무리 없는 수준으로 얹어 교육 범위에 맞는 API 서버로 전환한다."**
