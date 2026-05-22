# FW_10_rest_lab 분석 기반 인사이트

## 1. 결론 요약

`FW_10_rest_lab`를 기준으로 판단하면, 현재 교육 범위의 중심은 다음 조합에 있다.

- Spring Boot MVC + REST Controller 병행
- 세션 기반 로그인
- Interceptor를 이용한 인증 체크
- AOP를 이용한 공통 로깅
- Swagger(OpenAPI) 문서화
- `ResponseEntity` 기반 REST 응답
- `ProblemDetail` 기반 REST 예외 응답
- `WebMvcTest` + Mockito 기반 컨트롤러 테스트
- MyBatis 또는 단순 Repository/Service 계층 활용

반면, 참고 프로젝트에는 **Spring Security 자체가 들어가 있지 않다**.

- `spring-boot-starter-security` 없음
- `SecurityFilterChain` 없음
- `PasswordEncoder` 없음
- `UserDetailsService` 없음
- JWT/OAuth2 없음

즉, 이번 작업에서 Spring Security를 도입하더라도, 참고 프로젝트의 교육 범위를 벗어나지 않으려면 **PasswordEncoder + 단순한 URL 보호 + 세션 기반 흐름** 정도에서 멈추는 것이 적절하다.

## 2. 참고 프로젝트에서 실제로 확인된 구조

## 2.1 의존성과 기술 스택

참고 프로젝트 `FW_10_rest_lab/pom.xml`에서 확인되는 중심 기술은 다음과 같다.

- `spring-boot-starter-web`
- `spring-boot-starter-aop`
- `mybatis-spring-boot-starter`
- `springdoc-openapi-starter-webmvc-ui`
- JSP/JSTL
- `spring-boot-starter-test`

여기서 중요한 점은:

- AOP는 교육 범위 안에 있다.
- Swagger는 교육 범위 안에 있다.
- REST API + MVC 병행은 교육 범위 안에 있다.
- 하지만 **Spring Security는 참고 프로젝트 범위에 포함되어 있지 않다.**

## 2.2 인증 구조

참고 프로젝트의 인증 방식은 전형적인 "간단한 세션 로그인"이다.

- `BootMemberController`에서 로그인 처리
- `HttpSession`에 `loginUser` 저장
- `SessionInterceptor`가 `/auth/**` 요청을 가로채 로그인 여부 확인
- 미로그인 시 로그인 폼으로 리다이렉트

이 구조는 다음 특징을 가진다.

- Security Framework 없이 인증 흐름을 직접 구현
- 세션과 인터셉터를 사용
- REST API 인증 체계는 별도로 정교화하지 않음

즉, 수업 기준선은 "세션 로그인 + 인터셉터"까지는 분명히 포함되며, Security는 아직 강의 레퍼런스의 핵심 주제가 아니다.

## 2.3 비밀번호 처리 수준

참고 프로젝트는 비밀번호를 평문으로 저장하고 비교한다.

- DB 스키마의 `password` 컬럼에 평문 저장
- `BasicMemberService.login()`에서 `member.getPassword().equals(password)` 비교
- `PasswordEncoder` 사용 없음

이 점은 두 가지를 뜻한다.

1. 참고 프로젝트는 보안 완성도보다 흐름 이해를 우선한 교육용 예제다.
2. 현재 프로젝트에서 `PasswordEncoder`를 도입하는 것은 참고 프로젝트보다 한 단계 높은 개선이지만, 여전히 충분히 교육 범위 안에서 설명 가능한 수준이다.

## 2.4 REST API 스타일

참고 프로젝트의 REST API 스타일은 아래처럼 단순하다.

- `@RestController`
- `@RequestMapping("/api/v1/...")`
- `ResponseEntity<?>`
- 응답 본문은 `Map.of(...)`
- 일부 요청은 `@RequestBody`, 일부는 `@ModelAttribute`, `@RequestParam`, `@PathVariable`

예:

- `POST /api/v1/members`
- `GET /api/v1/members/{email}`
- `GET /api/v1/members`
- `PUT /api/v1/members`

이 구조가 보여주는 교육적 메시지는 명확하다.

- REST는 리소스 경로와 HTTP 메서드로 설계한다.
- 응답은 복잡한 공통 래퍼 없이도 충분하다.
- DTO를 쓰되 지나치게 많은 abstraction은 필요 없다.

## 2.5 예외 처리 방식

참고 프로젝트는 MVC와 REST 예외 처리를 분리한다.

- `MvcExceptionHandler`: `@Controller`용
- `RestExceptionHandler`: `@RestController`용

REST 쪽은 `ProblemDetail`을 반환한다.

이 패턴은 현재 프로젝트에도 매우 잘 맞는다.

- JSP 화면용 예외 처리와
- `/api/v1/**` REST 예외 처리를
- 분리해서 운영하는 방식

이는 지금 레포를 고도화할 때 그대로 가져와도 좋다.

## 2.6 공통 관심사 처리 방식

참고 프로젝트는 공통 관심사를 두 갈래로 분리한다.

### Interceptor

- `SessionInterceptor`: 로그인 체크
- `PerformanceInterceptor`: 요청 시간과 파라미터 로깅

### Aspect

- `LoggingAspect`: DAO 호출 전 메서드/인자 로깅

여기서 교육 범위상 중요한 포인트는 다음과 같다.

- "요청 단위 공통 처리"는 Interceptor
- "메서드 단위 공통 처리"는 Aspect

즉, `aspect` 패키지를 추가해 로깅을 담당하게 하는 것은 **참고 프로젝트와도 정합적**이다.

반대로 비밀번호 인코딩은 참고 프로젝트의 공통 처리 패턴과 맞지 않는다.  
비밀번호 인코딩은 비즈니스/보안 규칙에 가깝기 때문에 AOP보다 서비스 레이어의 명시적 처리로 두는 편이 낫다.

## 2.7 Swagger 적용 수준

참고 프로젝트는 Swagger를 과하게 쓰지 않는다.

- `@OpenAPIDefinition`
- `GroupedOpenApi`
- `@Tag`
- `@Operation`
- 일부 DTO에 `@Schema`

즉, 문서화 수준도 "충분히 이해 가능한 정도"에서 멈춘다.

이건 현재 프로젝트에도 그대로 적용하면 된다.

- 모든 응답 케이스를 장황하게 적지 않는다.
- 핵심 API 위주로 태그와 설명을 붙인다.
- REST API만 문서화한다.

## 2.8 테스트 수준

참고 프로젝트는 `WebMvcTest` + `MockitoBean` 기반의 컨트롤러 슬라이스 테스트를 사용한다.

테스트 특징:

- 컨트롤러만 로드
- 서비스는 목(mock) 처리
- JSON 응답 필드 검증
- 상태 코드 검증

이 역시 교육 범위의 상한을 잘 보여준다.

- 통합 테스트를 과도하게 늘리기보다
- REST Controller 단위 테스트 몇 개를 확실히 두는 방식

## 3. 참고 프로젝트의 한계와 해석

참고 프로젝트는 교육용 예제로서 충분히 유용하지만, 설계적으로 완벽하지는 않다.

대표적으로:

- REST Controller 이름이 다소 혼란스럽다
  - `AuthRestController`가 실제로는 member 조회/수정 API를 많이 담당
- 응답을 대부분 `Map.of(...)`로 반환한다
- 비밀번호는 평문이다
- 인증은 세션/인터셉터 수준에 머문다
- `refresh` 필드는 스키마/DTO에 있지만 실제 Security 또는 JWT 구조는 없다

이 한계는 오히려 현재 프로젝트 설계 시 중요한 힌트가 된다.

- 교육 범위는 넓지 않다.
- Security를 도입하더라도 "보여주기 위한 고급 설계"보다 "기초 원리 + 명시적 흐름"이 더 중요하다.

## 4. 현재 레포에 적용할 때의 기술 상한

현재 레포에 Spring Security를 넣더라도, 교육 범위를 고려한 상한은 아래 정도가 적절하다.

## 4.1 해도 되는 것

### 1. `spring-boot-starter-security` 추가

이건 이번 작업의 필수 범위에 포함시켜도 무리가 없다.

### 2. `PasswordEncoder` 빈 등록

예:

- `BCryptPasswordEncoder`

이건 반드시 도입하는 것이 좋다.  
평문 비밀번호 비교는 현재 프로젝트에서 가장 먼저 개선해야 할 부분이다.

### 3. `SecurityFilterChain` 1개 구성

아래 정도의 단순한 설정은 교육 범위 안에서 충분히 소화 가능하다.

- Swagger 경로 permit
- 정적 리소스 permit
- 회원가입/로그인 permit
- 나머지 인증 필요
- 세션 기반 유지

### 4. 세션 기반 인증 유지

JWT 없이도 괜찮다.  
오히려 현재 수업 기준으로는 세션 기반이 더 자연스럽다.

### 5. `@RestControllerAdvice` + `ProblemDetail` 또는 단순 `ErrorResponse`

참고 프로젝트의 연장선으로 매우 적절하다.

### 6. `aspect` 패키지 도입

특히 아래 용도로 적합하다.

- Controller/Service/Repository 로깅
- 실행 시간 측정
- 예외 로그

## 4.2 이번 범위를 넘어설 가능성이 큰 것

아래는 이번 작업의 기술 상한을 넘길 가능성이 크므로 지양하는 것이 좋다.

### 1. JWT/Refresh Token 체계

- 토큰 발급
- 토큰 재발급
- Redis 저장
- Access/Refresh 분리

이건 참고 프로젝트가 전혀 보여주지 않는 범위다.

### 2. OAuth2 로그인

이번 프로젝트 목적과도 거리가 멀다.

### 3. 복잡한 `UserDetailsService`/`AuthenticationProvider` 커스터마이징

기본 학습 범위를 넘길 수 있다.

### 4. Method Security 남발

- `@PreAuthorize`
- `@PostAuthorize`

권한 체계가 아직 단순하므로 과하다.

### 5. Password 인코딩을 AOP로 숨기는 방식

흐름이 보이지 않고, 이중 인코딩 위험이 있으며, 교육용으로도 설명이 불친절하다.

## 5. 현재 레포에 맞는 권장 설계

## 5.1 보안 구조

현재 레포에는 이미 다음 구조가 있다.

- `AuthService`: 로그인/회원가입
- `LoginCheckFilter`: 인증 체크
- `SessionUtils`: 세션 유틸
- `UserRepository`: 사용자 조회/저장

여기에 Spring Security를 넣을 때 가장 중요한 원칙은 다음이다.

**인증 책임의 주체를 중복시키지 않는다.**

즉:

- `LoginCheckFilter`
- Spring Security

둘이 동시에 URL 보호를 강하게 담당하면 흐름이 꼬인다.

### 권장 방향

장기적으로는 `LoginCheckFilter`의 역할을 `SecurityFilterChain`으로 이관하는 것이 좋다.

초기 단계에서 가능한 현실적인 선택은 두 가지다.

#### 선택지 A. 최소 침습

- `PasswordEncoder`만 먼저 도입
- 기존 세션 로그인/필터 구조 유지
- REST API 추가와 함께 Security 적용 범위 확대

장점:

- 현재 코드 변경 폭이 작다.

단점:

- Spring Security를 "적극 활용"한다고 보기는 약하다.

#### 선택지 B. 교육 범위 내 적극 활용

- `spring-boot-starter-security` 추가
- `SecurityFilterChain` 구성
- 공개/보호 경로 정의
- 세션 기반 인증 유지
- `PasswordEncoder` 도입
- `LoginCheckFilter`는 점진 제거 또는 축소

장점:

- Security를 실제 접근 제어에 활용한다.

단점:

- 설정과 흐름 정리가 필요하다.

**이번 요구사항을 고려하면 선택지 B가 더 적절하다.**

## 5.2 PasswordEncoder 적용 원칙

비밀번호 처리 규칙은 서비스 계층에 명시적으로 둔다.

### 적용 지점

- 회원가입 시 `encode()`
- 로그인 시 `matches()`
- 프로필 수정에서 비밀번호 변경 시 `encode()`
- Seed/Demo 계정도 해시값으로 저장

### 적용하지 말아야 할 방식

- 저장 직전에 AOP로 자동 인코딩
- Repository에서 몰래 인코딩
- 컨트롤러에서 직접 인코딩

### 이유

- 서비스가 도메인 규칙을 가장 잘 드러낸다.
- 교육용으로도 흐름 설명이 쉽다.
- 테스트가 단순해진다.

## 5.3 패키지 설계 권장안

현재 레포에는 아래 패키지 추가가 적절하다.

```text
src/main/java/com/ssafy/yumyum
├── aspect
│   └── LoggingAspect.java
├── config
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/api
├── dto
├── exception
│   └── ApiExceptionHandler.java
└── service
```

### 역할 분리

- `aspect`
  - 로깅, 실행 시간, 예외 추적
- `config`
  - `SecurityFilterChain`
  - `PasswordEncoder` 빈
  - Swagger 설정
- `service`
  - 비밀번호 인코딩/비교 로직

## 5.4 REST 응답 스타일 권장안

참고 프로젝트처럼 `ResponseEntity` 중심은 유지해도 좋다.

다만 현재 프로젝트에서는 아래 정도로 약간 더 정리하는 편이 낫다.

- 성공 응답: DTO 직접 반환 또는 `Map.of("data", ...)` 수준
- 오류 응답: `ProblemDetail` 또는 `ErrorResponse`

교육용 프로젝트이므로 복잡한 `ApiResponse<T>` 강제 래퍼는 필수가 아니다.

## 5.5 테스트 상한

이번 범위에서는 아래 정도면 충분하다.

- `AuthApiController` 로그인/회원가입 테스트
- 비밀번호 인코딩 검증
- 보호된 API 미인증 접근 테스트
- 식단 API 1~2개 테스트

즉, `WebMvcTest` + Mockito 기반 테스트가 가장 적절하다.

## 6. 이번 레포에 대한 구체적 설계 제안

참고 프로젝트 분석을 기준으로 보면, 지금 레포의 다음 설계가 가장 현실적이다.

## 6.1 필수

- `pom.xml`에 `spring-boot-starter-security` 추가
- `pom.xml`에 Swagger, AOP 의존성 추가 검토
- `SecurityConfig` 생성
- `PasswordEncoder` 빈 등록
- `AuthService.login()`에서 `matches()` 사용
- `AuthService.register()`에서 `encode()` 사용
- `ProfileController` 또는 `UserService.updateProfile()`에서 비밀번호 변경 시 `encode()` 사용

## 6.2 강하게 권장

- `aspect/LoggingAspect` 추가
- `@RestControllerAdvice` 추가
- `/api/v1/**` 기준 Swagger 설정 추가
- 장기적으로 `LoginCheckFilter` 책임을 Security로 이관

## 6.3 지금은 하지 않는 것이 좋은 것

- JWT 발급/재발급
- `UserDetailsService`를 중심으로 한 복잡한 인증 아키텍처
- OAuth2
- role hierarchy
- 너무 많은 Security 커스터마이징

## 7. 최종 판단

`FW_10_rest_lab`는 "Spring Security 수업 레퍼런스"라기보다, **REST API + Swagger + AOP + Interceptor + 예외 처리** 수업 레퍼런스에 가깝다.

따라서 이 레포에서 Spring Security를 도입하더라도, 다음 선을 넘지 않는 것이 좋다.

- `PasswordEncoder`는 반드시 도입
- `SecurityFilterChain`으로 공개/보호 경로를 단순하게 관리
- 인증 상태는 세션 기반으로 유지 가능
- 비밀번호 로직은 서비스에서 명시 처리
- 로깅은 `aspect` 패키지에서 담당
- JWT/OAuth2/복잡한 인증 프레임워크는 이번 범위 밖

즉, 이번 작업의 가장 적절한 목표는 다음 한 문장으로 정리할 수 있다.

**"참고 프로젝트가 보여준 단순한 REST/AOP/Swagger 스타일 위에, Spring Security는 PasswordEncoder와 기본 접근 제어 수준까지만 얹는다."**
