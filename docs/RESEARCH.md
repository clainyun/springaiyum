# YumYumCoach Repository Research

## 1. 개요

이 레포는 기존 Servlet/JSP 기반 프로젝트를 Spring Boot + Spring MVC 구조로 옮긴 상태다.  
컨트롤러, 서비스, 리포지토리, JSP 뷰로 계층 분리가 되어 있고, 일부 도메인은 JDBC 기반으로 MySQL을 사용하지만 일부 도메인은 아직 메모리 저장소에 머물러 있다.

핵심적으로 보면 다음과 같이 나뉜다.

| 영역 | 상태 | 비고 |
| --- | --- | --- |
| 인증, 회원, 식단, 음식 카탈로그 | JDBC 사용 | `UserRepository`, `MealRepository`, `FoodCatalogRepository` |
| 커뮤니티 | JDBC 사용 | `CommunityRepository`가 자체적으로 테이블 생성 |
| 챌린지, 소셜 | 미포팅 | `Map` + `SeedDataFactory` 기반 메모리 저장소 |
| AI 코치, 홈 대시보드 | 혼합 상태 | 식단은 JDBC, 챌린지/소셜은 메모리 데이터에 의존 |

## 2. 전반적인 구조

### 2.1 패키지 구조

| 패키지 | 역할 |
| --- | --- |
| `controller` | HTTP 요청 수신, 파라미터 파싱, 뷰/리다이렉트/JSON 응답 결정 |
| `service` | 유효성 검증, 정렬, 추천, 분석 등 비즈니스 로직 |
| `repository` | DB 접근 또는 메모리 저장소 접근 |
| `model` | 도메인 모델 |
| `filter` | 로그인 검사, 플래시 메시지 노출, `currentUser` 주입 |
| `exception` | 공통 예외 및 오류 화면 처리 |
| `util` | 세션, 정렬, 뷰 헬퍼, ID 생성, DB 연결 등 공용 유틸 |
| `src/main/webapp/WEB-INF/views` | JSP 화면 |

### 2.2 요청 흐름

1. 브라우저가 Controller 엔드포인트 호출
2. `LoginCheckFilter`가 로그인 필요 여부를 판단
3. 로그인된 경우 `currentUser`를 request attribute로 주입
4. Controller가 Service 호출
5. Service가 Repository를 통해 데이터 조회/수정
6. 결과를 JSP View, Redirect, 또는 JSON으로 반환

### 2.3 인증/세션 방식

- 세션 키는 `loginUserId`
- 플래시 메시지는 `SessionUtils.flash()`로 세션에 저장 후 다음 요청에서 `SessionUtils.exposeFlash()`로 request에 노출
- 대부분의 화면은 세션 로그인 기반
- JSON이 기대되는 요청은 미인증 시 `401` JSON 응답을 내려준다

## 3. 도메인별 구현 현황

| 도메인 | 주요 클래스 | 저장 방식 | JDBC 포팅 상태 | 메모 |
| --- | --- | --- | --- | --- |
| 인증 | `AuthController`, `AuthService`, `UserRepository` | MySQL | 완료 | 로그인/회원가입/로그아웃 |
| 프로필 | `ProfileController`, `UserRepository` | MySQL | 완료 | 수정, 비활성화 |
| 식단 | `MealController`, `MealService`, `MealRepository` | MySQL | 완료 | `meals`, `meal_foods` 사용 |
| 음식 카탈로그 | `MealService`, `FoodCatalogRepository` | MySQL | 완료 | `food_nutrition` 조회 전용 |
| 커뮤니티 | `CommunityController`, `CommunityService`, `CommunityRepository` | MySQL | 완료 | `community_posts`, `community_comments` 자동 생성 |
| 챌린지 | `ChallengeController`, `ChallengeService`, `ChallengeRepository` | 메모리 | 미완료 | `Map<String, Challenge>`, `Map<String, ChallengeMembership>` |
| 소셜 | `SocialController`, `SocialService`, `SocialRepository` | 메모리 | 미완료 | `Map<String, FollowRelation>` |
| 홈/코치 | `HomeController`, `CoachController`, `CoachService` | 혼합 | 부분 완료 | 식단은 JDBC, 챌린지/소셜은 메모리 |

## 4. JDBC 포팅 관점의 핵심 발견

### 4.1 아직 JDBC로 포팅되지 않은 영역

#### 챌린지

- `ChallengeRepository`는 `DBUtil`이나 SQL을 전혀 사용하지 않는다.
- 내부 구조가 `LinkedHashMap` 두 개다.
  - `Map<String, Challenge> challenges`
  - `Map<String, ChallengeMembership> memberships`
- 애플리케이션 재시작 시 데이터가 사라진다.
- 따라서 챌린지 생성, 참여, 진행률 수정, 탈퇴, 삭제는 모두 휘발성이다.

#### 소셜

- `SocialRepository` 역시 DB 접근 없이 `Map<String, FollowRelation>`만 사용한다.
- `follow`, `unfollow`, 추천 사용자, 리더보드가 모두 메모리 관계 데이터에 의존한다.
- 애플리케이션 재시작 시 팔로우 관계가 유지되지 않는다.

### 4.2 JDBC는 되었지만 외부 SQL과 어긋나는 영역

#### 사용자 테이블 불일치

- 코드의 `UserRepository`는 `users.user_id`를 `VARCHAR(64)`로 가정한다.
- 반면 `SSAFY_COACH_Schema.sql`과 `assets/ssafy_yumyumcoach.sql`은 `users.user_id`를 `INT AUTO_INCREMENT`로 정의한다.
- 서비스 코드는 `IdGenerator.next("USER")`로 문자열 ID를 만든다.
- 즉 README대로 외부 SQL을 먼저 실행하면 신규 회원가입 시 문자열 ID 저장이 충돌할 가능성이 높다.

#### 식단 테이블 불일치

- 코드의 `MealRepository`는 `meals`, `meal_foods` 테이블을 직접 생성하고 사용한다.
- 하지만 제공 SQL은 `diet_logs`, `diet_log_items`를 정의한다.
- 결과적으로 외부 SQL의 식단 데이터는 현재 코드가 읽지 않는다.
- README의 실행 방법대로 덤프를 넣어도, 실제 런타임은 별도의 `meals`, `meal_foods`를 새로 만들고 그쪽을 사용한다.

#### 커뮤니티 테이블 누락

- 커뮤니티는 JDBC로 구현되어 있지만 `SSAFY_COACH_Schema.sql`에는 `community_posts`, `community_comments` 정의가 없다.
- 대신 `CommunityRepository`가 애플리케이션 시작 중 자체적으로 테이블을 생성한다.
- 즉 문서 기준 스키마와 실제 런타임 스키마가 다르다.

### 4.3 JDBC 포팅은 되었지만 Spring 방식으로는 덜 정리된 영역

- 모든 JDBC 접근이 Spring `DataSource` 주입이 아니라 `DBUtil.getConnection()` 정적 호출을 사용한다.
- `application.properties`에 datasource 설정이 있지만, 실제 리포지토리는 이를 직접 사용하지 않는다.
- `DBUtil`에는 URL/USER/PASSWORD가 하드코딩되어 있다.
- 따라서 설정이 두 군데에 중복되고, 환경별 설정 전환이 어렵다.

## 5. Seed 데이터와 초기화 방식

### 5.1 `SeedDataFactory`의 역할

`SeedDataFactory`는 사실상 두 가지 역할을 동시에 가진다.

1. JDBC 테이블이 비어 있을 때 초기 데이터를 넣는 역할
2. 아직 JDBC로 포팅되지 않은 도메인의 유일한 저장소 역할

### 5.2 도메인별 사용 방식

| 도메인 | `SeedDataFactory` 사용 방식 |
| --- | --- |
| User | DB가 비어 있으면 초기 사용자 insert |
| Meal | DB가 비어 있으면 초기 식단 insert |
| Community | DB가 비어 있으면 초기 게시글/댓글 insert |
| Challenge | 애플리케이션 메모리 저장소 초기값 |
| Social | 애플리케이션 메모리 저장소 초기값 |

이 구조 때문에 `SeedDataFactory`가 단순 테스트 데이터 수준을 넘어 실제 운영 흐름 일부를 담당하고 있다.

## 6. 레거시 Servlet 흔적

다음 파일들은 Spring MVC 구조로 옮긴 뒤에도 남아 있는 레거시 자산이다.

| 파일/구성 | 상태 | 의미 |
| --- | --- | --- |
| `BaseController` | 사실상 미사용 | `HttpServlet` 기반 공통 컨트롤러 |
| `AppContainer` | 사실상 미사용 | 정적 Bean 조회 컨테이너 |
| `web.xml` | 잔존 | JSP 인코딩, welcome-file 설정 |
| `src/main/webapp/index.jsp` | 잔존 | `/home`으로 리다이렉트 |

해당 파일들은 지금 구조에서 꼭 필요하지는 않지만, 이전 MVC/Servlet 프로젝트의 이행 흔적으로 보인다.

## 7. 기능별 세부 메모

### 7.0 계층 사용 일관성

- 패키지 구조는 `controller -> service -> repository`로 나뉘어 있지만, 실제 호출 관계는 완전히 일관되지는 않다.
- 예를 들어 `HomeController`는 `UserRepository`를 직접 사용하고, `ProfileController`도 프로필 수정 로직 상당 부분을 직접 처리한다.
- 반면 `UserService`에는 `updateProfile()`, `deactivate()`, `delete()` 같은 메서드가 존재하지만 현재 컨트롤러에서 적극적으로 활용되지는 않는다.
- 즉 구조는 Spring스럽게 분리되어 있지만, 일부 화면은 아직 "컨트롤러가 검증과 조립을 많이 담당하는" 과도기 상태로 보인다.

### 7.1 인증/회원

- 비밀번호는 평문 비교다.
- 가입/프로필 수정 시 이메일 중복 검증을 서비스 또는 컨트롤러에서 수행한다.
- 계정 삭제 액션은 실제 삭제가 아니라 비활성화로 처리된다.

### 7.2 식단

- 식단 필터링과 정렬 일부가 SQL이 아니라 서비스 레이어 메모리 연산으로 수행된다.
- 음식 검색은 `food_nutrition`을 직접 조회한다.
- 추천 음식은 `FoodCatalogRepository.findAll()` 후 에너지 차이를 계산해서 추천한다.

### 7.3 커뮤니티

- 게시글/댓글은 JDBC로 저장된다.
- 게시글과 댓글의 작성자명 조회는 `UserRepository.findById()`를 반복 호출하는 구조라 N+1 성격이 있다.

### 7.4 코치/홈

- `CoachService`는 식단 영양 요약과 사용자 목표를 바탕으로 코멘트를 만든다.
- 하지만 챌린지 관련 정보는 아직 메모리 저장소를 통해 가져온다.

### 7.5 소셜/챌린지

- 정렬, 추천, 리더보드, 참여 현황 모두 메모리 리스트 기반 계산이다.
- 데이터량이 많아질 경우 비효율적이며 영속성도 없다.

## 8. 문서와 실제 코드 사이의 불일치

### 8.1 README의 테스트 계정 불일치

- README: `demo@yumyum.com`
- 실제 코드/덤프:
  - `demo@yumyam.com`
  - `demo@yamyam.com`

즉 문서상 데모 계정 이메일과 실제 초기 데이터가 서로 다르다.

### 8.2 README의 DB 설명 불일치

- README는 `users`, `meals`, `meal_foods`, `food_nutrition` 중심으로 설명한다.
- 실제 배포용 SQL은 `diet_logs`, `diet_log_items`를 사용한다.
- 커뮤니티 테이블은 README DB 섹션에서 빠져 있고, 챌린지/소셜 테이블 정의도 없다.

## 9. 기술 부채 및 우선순위 제안

### 우선순위 1

스키마를 하나로 통일해야 한다.

- `users.user_id` 타입 통일
- 식단 테이블명 통일: `diet_logs/diet_log_items` 또는 `meals/meal_foods`
- 커뮤니티/챌린지/소셜 테이블 정의를 공식 스키마에 포함

### 우선순위 2

`ChallengeRepository`, `SocialRepository`를 JDBC 기반으로 포팅해야 한다.

필요 테이블 예시:

- `challenges`
- `challenge_memberships`
- `follow_relations`

### 우선순위 3

`DBUtil`을 제거하고 Spring `DataSource` 또는 `JdbcTemplate` 기반으로 정리하는 것이 좋다.

- 설정 중복 제거
- 테스트 용이성 향상
- 환경별 DB 설정 일원화

### 우선순위 4

레거시 자산 정리가 필요하다.

- `BaseController`
- `AppContainer`
- `web.xml`
- 불필요한 welcome JSP

### 우선순위 5

테스트 코드 추가가 필요하다.

- 인증/회원가입
- 식단 CRUD
- 커뮤니티 CRUD
- 로그인 필터
- 챌린지/소셜 JDBC 포팅 이후 리포지토리 테스트

## 10. 결론

이 프로젝트는 "Spring MVC 구조 전환" 자체는 상당 부분 진행되었지만, "저장 계층의 JDBC 포팅"은 도메인별 완성도가 다르다.

- 완료 또는 거의 완료: 회원, 식단, 음식 카탈로그, 커뮤니티
- 미완료: 챌린지, 소셜
- 구조적 불일치: 외부 SQL 스키마와 실제 런타임 리포지토리 정의

따라서 다음 작업의 핵심은 화면이나 컨트롤러 추가보다 먼저, 스키마와 리포지토리의 저장 전략을 일관되게 맞추는 것이다.
