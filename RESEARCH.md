# dbyum 프로젝트 조사 보고서

작성 기준일: 2026-05-08
조사 범위: `c:\Users\SSAFY\git\dbyum` 현재 작업 디렉터리 전체

## 한눈에 보는 결론

- 이 프로젝트는 `Spring` 없이 `Jakarta Servlet + JSP + 수동 MVC`로 구성된 식단/소셜/챌린지 웹앱이다.
- 런타임 구조는 `controller -> service -> repository -> model` 계층이 비교적 선명하며, `AppContainer`가 정적 싱글턴 DI 역할을 한다.
- 실제 실행 데이터는 현재 `SeedDataFactory` 기반의 메모리 저장소를 사용하며, 서버 재시작 시 초기화된다.
- 동시에 `MySQL JDBC` 의존성과 `DBUtil`도 남아 있어 DB 전환 흔적이 보이지만, 현재 코드 경로에서는 사용되지 않는다.
- 화면(JSP) 10개가 `com.ssafy.yumyum`이 아니라 `com.ssafy.yumyum` 패키지를 import하고 있어, 컨테이너에서 JSP 컴파일 시 실패할 가능성이 매우 높다.
- 문서와 실제 코드가 많이 어긋나 있다. README는 `Java 8 / Tomcat 9 / com.ssafy.yumyum` 기준인데, 현재 작업 트리의 `pom.xml`은 `Java 17`, `war`, `Jakarta Servlet 6.1` 쪽으로 맞춰져 있다.

## 1. 현재 작업 트리 상태

이번 조사는 "현재 디렉터리의 실제 상태"를 기준으로 했다. 즉, HEAD 기준 저장소뿐 아니라 아직 커밋되지 않은 로컬 변경도 포함한다.

### 확인된 작업 트리 상태

- 조사 중 Git 작업 트리 상태가 한 차례 변동했다.
- 적어도 Eclipse 계열 산출물로 보이는 `bin/`이 추적 밖에 존재함을 확인했다.

### 의미

- 현재 저장소는 완전히 깨끗한 상태가 아니며, 환경 의존적인 산출물과 로컬 변경이 섞일 수 있는 상태였다.
- 따라서 "문서상 프로젝트"와 "지금 디렉터리에 놓인 프로젝트"를 분리해서 봐야 한다.
- 이 보고서는 후자를 기준으로 작성했다.

## 2. 프로젝트 개요

이 프로젝트는 식단 기록과 분석을 중심으로 다음 기능을 묶은 학습형 웹앱이다.

- 인증: 로그인, 회원가입, 로그아웃
- 홈 대시보드: 오늘 섭취 요약, 최근 식단, 챌린지 요약, 코치 요약
- 식단: 목록, 상세, 등록, 수정, 삭제, 음식 추천, 영양 분석
- 프로필: 개인정보 수정, 계정 비활성화, 계정 삭제
- 소셜: 팔로우/언팔로우, 추천 사용자, 팔로워/팔로잉, 리더보드
- 챌린지: 생성, 참여, 탈퇴, 진행률 업데이트, 삭제
- 커뮤니티: 게시글/댓글 CRUD
- AI 코치: 최근 식단 분석 기반 요약과 운동 루틴 제안

루트에는 기능별 화면 시안으로 보이는 PNG도 함께 있다.

- `메인화면.png`
- `식단기록.png`
- `소셜.png`
- `챌린지.png`
- `커뮤니티.png`

## 3. 기술 스택과 실행 조건

### 실제 코드 기준

- 웹 프레임워크: `Jakarta Servlet`
- 뷰: `JSP`
- 스타일링: `Bootstrap 5`, `Bootstrap Icons`, 커스텀 CSS
- 빌드 형태: `Maven WAR`
- 데이터 저장: 현재는 `In-Memory Repository`
- JDBC 흔적: `MySQL Connector/J`, `DBUtil`

### 현재 `pom.xml`

현재 `pom.xml`은 다음 방향을 가진다.

- `packaging` = `war`
- Java 컴파일 타깃 = `17`
- `jakarta.servlet-api:6.1.0`
- `jakarta.servlet.jsp-api:4.0.0`
- JSTL API/구현체
- MySQL 드라이버
- Lombok

### `web.xml` 기준 런타임 기대치

- `web-app version="6.1"`
- JSP/JSPF 인코딩 UTF-8
- welcome file은 `index.jsp`

즉, 실제 웹 컨테이너는 `Jakarta EE` 네임스페이스를 이해해야 한다. README에 적힌 `Tomcat 9`는 `javax.servlet` 계열이라 현재 코드와 맞지 않는다.

## 4. 문서와 실제 코드의 불일치

이번 조사에서 가장 눈에 띈 부분 중 하나다.

### README와 현재 코드의 차이

- README는 `Java 8`을 안내하지만, 현재 `pom.xml`은 `Java 17`이다.
- README는 `Tomcat 9 이상`을 말하지만, 실제 코드는 `jakarta.servlet.*`를 사용한다.
- README는 패키지 경로를 `com/yamyam`으로 설명하지만, 실제 Java 패키지는 `com.ssafy.yumyum`이다.
- README는 배포 URL을 `yamyam-mvc-1.0.0/home`로 예시하지만, 현재 `artifactId`와 `finalName`은 `dbyum` 계열이다.

## 5. 디렉터리 구조 요약

### Java 소스 수

- controller: 8개
- service: 7개
- repository: 6개
- model: 15개
- util: 9개
- 총 Java 파일: 45개

### View 수

- `WEB-INF/views` 하위 JSP/JSPF: 15개
- 전체 `src/main/webapp` 파일 수: 18개

### 테스트

- `src/test` 디렉터리가 없다.
- 자동 테스트 코드는 현재 없는 상태로 봐도 무방하다.

## 6. 런타임 아키텍처

### 6.1 라우팅 방식

각 컨트롤러가 `@WebServlet`으로 직접 URL을 매핑한다. 프런트 컨트롤러 패턴이 아니라 기능별 서블릿 분산 구조다.

주요 경로는 다음과 같다.

| 경로 | 컨트롤러 | 역할 |
| --- | --- | --- |
| `/auth/login` | `AuthController` | 로그인 화면/처리 |
| `/auth/signup` | `AuthController` | 회원가입 화면/처리 |
| `/auth/logout` | `AuthController` | 로그아웃 |
| `/home` | `HomeController` | 대시보드 |
| `/meals` | `MealController` | 식단 목록 |
| `/meals/new` | `MealController` | 식단 등록 |
| `/meals/detail` | `MealController` | 식단 상세 |
| `/meals/edit` | `MealController` | 식단 수정 |
| `/meals/delete` | `MealController` | 식단 삭제 |
| `/profile` | `ProfileController` | 프로필 조회/수정/계정 관리 |
| `/social` | `SocialController` | 소셜 |
| `/challenges` | `ChallengeController` | 챌린지 |
| `/community` | `CommunityController` | 커뮤니티 |
| `/coach` | `CoachController` | AI 코치 |

루트 `index.jsp`는 단순히 `/home`으로 리다이렉트한다.

### 6.2 공통 베이스 컨트롤러

`BaseController`가 모든 컨트롤러의 공통 부모다.

- 요청/응답 인코딩을 UTF-8로 고정
- `SessionUtils.exposeFlash(req)`로 flash message를 request attribute로 노출
- `render()`가 `/WEB-INF/views/...jsp`로 forward
- `redirect()`가 context path를 붙여 redirect
- `requireLoginUser()`가 로그인 여부와 활성 사용자 여부를 검사

즉, 인증이 필요한 화면은 각 컨트롤러가 `requireLoginUser()`를 호출하는 방식이다.

### 6.3 수동 DI

`AppContainer`가 애플리케이션 전역 싱글턴을 보관한다.

- Repository 인스턴스를 정적으로 생성
- Service 인스턴스를 정적으로 생성
- `SeedDataFactory`로 초기 데이터 삽입

스프링 같은 컨테이너는 없고, 사실상 "작은 서비스 로케이터"에 가깝다.

## 7. 계층별 상세 분석

### 7.1 Controller 계층

특징:

- URL 분기와 request parameter 파싱 담당
- 권한 검사는 대부분 컨트롤러에서 1차 수행
- ServiceResult를 받아 flash 또는 errorMessage로 UI 피드백 연결

관찰:

- `MealController`, `ProfileController`, `CommunityController`, `ChallengeController`, `SocialController`, `CoachController`, `HomeController`는 모두 로그인 필요
- `AuthController`는 로그인된 사용자가 `/auth/login`, `/auth/signup` 접근 시 `/home`으로 보냄
- 삭제/수정 관련 소유권 체크는 주로 서비스와 컨트롤러가 함께 담당

### 7.2 Service 계층

비즈니스 규칙이 가장 많이 몰린 레이어다.

#### AuthService

- 이메일 존재 여부 검사
- 활성 계정 여부 검사
- 비밀번호 평문 비교
- 회원가입 시 최소한의 유효성 검사

#### MealService

핵심 역할:

- 식단 필터링/정렬
- 식단 생성/수정/삭제
- 영양 요약 계산
- 일일 목표 계산
- 식단 점수화와 인사이트 생성
- 음식 추천

특히 중요한 규칙:

- `calculateDailyGoal()`은 Harris-Benedict 계열 공식으로 보이는 BMR 계산 후 목표별 보정
- `analyzeMeal()`은 칼로리 차이, 단백질 비중, 지방 비중으로 점수화
- `recommendFoods()`는 남은 목표 칼로리와 음식 칼로리 차이를 기준으로 추천

#### SocialService

- 팔로잉/팔로워 목록
- 추천 사용자
- 팔로워 수 기반 리더보드

추천/리더보드는 모두 "팔로워 수"가 핵심 기준이다.

#### ChallengeService

- 챌린지 생성
- 참여/탈퇴
- 진행률 업데이트
- 생성자만 삭제 가능
- 멤버십 맵/참가자 맵 생성

#### CommunityService

- 카테고리별 게시글 목록
- 게시글/댓글 CRUD
- 게시글/댓글 작성자 맵 생성
- 사용자의 식단 목록 조회

#### CoachService

여기는 진짜 AI 호출이 아니라 "규칙 기반 코칭 생성기"에 가깝다.

- 최근 식단 3개 분석
- 오늘 섭취 대비 요약 문구 생성
- 목표(goal)에 따라 운동 세션 추천
- 부족한 영양소나 챌린지 참여 상태를 바탕으로 next action 작성

### 7.3 Repository 계층

모든 저장소가 `Map` 또는 `List` 기반 메모리 저장소다.

- `UserRepository`: `Map<String, User>`
- `MealRepository`: `Map<String, Meal>`
- `SocialRepository`: `Map<String, FollowRelation>`
- `ChallengeRepository`: 챌린지/멤버십 각각 `Map`
- `CommunityRepository`: 게시글/댓글 각각 `Map`
- `FoodCatalogRepository`: 카탈로그 음식 `List`

특징:

- 대부분 `synchronized` 메서드 사용
- 간단한 동시성 보호는 있지만, 트랜잭션 개념은 없음
- 재시작 시 데이터 유지 안 됨

### 7.4 Model 계층

핵심 엔티티는 다음과 같다.

- `User`
- `Meal`
- `FoodItem`
- `NutritionSummary`
- `DailyGoal`
- `MealAnalysis`
- `FollowRelation`
- `Challenge`
- `ChallengeMembership`
- `ChallengeParticipant`
- `CommunityPost`
- `CommunityComment`
- `CoachAdvice`
- `WorkoutSession`
- `FoodRecommendation`

특이점:

- `FoodItem.copyWithGrams()`가 100g 기준 영양값을 선택 중량 기준으로 환산한다.
- `MealAnalysis`, `CoachAdvice`는 "계산 결과 DTO" 성격이 강하다.

## 8. 기능별 조사

### 8.1 인증

흐름:

- 로그인: 이메일 조회 -> 활성 상태 확인 -> 비밀번호 평문 비교
- 회원가입: 이메일 형식/중복, 비밀번호 길이, 닉네임 검사
- 로그아웃: 세션 invalidate 후 flash 설정

주의점:

- 비밀번호 해시가 없다.
- SeedDataFactory가 평문 비밀번호를 사용한다.

### 8.2 홈 대시보드

구성 요소:

- 최근 식단 3개
- 오늘 섭취 영양 요약
- 일일 목표
- AI 코치 요약
- 활성 챌린지 3개
- 소셜 수치

특징:

- 홈 화면은 다른 서비스의 계산 결과를 "조합"하는 성격이 강하다.
- 컨트롤러 자체 로직은 많지 않지만, 매 요청마다 여러 서비스 계산을 반복한다.

### 8.3 식단

지원 기능:

- 날짜/식사유형 필터
- 정렬 기준 선택
- 상세 분석
- 등록/수정/삭제
- 음식 검색
- 음식 추천

정렬 키:

- `dateDesc`
- `dateAsc`
- `energyDesc`
- `scoreDesc`

권한:

- 상세/수정/삭제 모두 meal owner인지 확인

### 8.4 프로필

지원 기능:

- 개인 정보 수정
- 목표/성별/신체 정보 변경
- 계정 비활성화
- 계정 영구 삭제

계정 삭제 시 정리되는 데이터:

- 본인 식단
- 팔로우 관계
- 본인이 생성한 챌린지와 멤버십
- 본인 게시글/댓글

즉, 수동 cascade delete를 서비스 레벨에서 구현했다.

### 8.5 소셜

지원 기능:

- 팔로우
- 언팔로우
- 추천 사용자
- 팔로워/팔로잉 목록
- 리더보드

관찰:

- `follow()`는 자기 자신 팔로우와 중복 팔로우만 막는다.
- 대상 사용자가 실제로 존재하는지, 활성 사용자인지 확인하지 않는다.

### 8.6 챌린지

지원 기능:

- 생성
- 참여
- 탈퇴
- 진행률 수정
- 삭제

관찰:

- 생성 시 제목, targetCount, endDate만 강하게 검사
- 참여 시 "이미 참여 중인지"와 "챌린지 존재 여부"만 검사
- 종료된 챌린지 참여 방지 로직은 없음

### 8.7 커뮤니티

지원 기능:

- 게시글 CRUD
- 댓글 CRUD
- 카테고리 필터
- 식단 연결

관찰:

- 게시글/댓글 수정/삭제는 작성자만 가능
- 하지만 `linkedMealId`가 실제로 본인 식단인지 검증하지 않는다.
- 따라서 이론상 다른 사용자의 meal id나 존재하지 않는 meal id도 연결될 수 있다.

### 8.8 AI 코치

실제 구조:

- 외부 AI API는 없다.
- 식단 분석 + 목표(goal) + 챌린지 상태를 조합한 규칙 기반 추천이다.

장점:

- 데모/학습용으로는 흐름이 명확하다.
- 서비스 레이어에 규칙이 응집돼 있어 유지보수 포인트가 분명하다.

한계:

- 개인화 깊이는 제한적
- 실제 운동/영양 코치라고 부르기엔 규칙이 단순함

## 9. 알고리즘 사용 위치

README 설명과 실제 코드가 어느 정도 맞아떨어진다.

### Quick Sort

- 구현 위치: `SortUtils.quickSort`
- 사용 위치:
  - 식단 정렬
  - 팔로워/팔로잉 정렬
  - 추천 사용자/리더보드 정렬
  - 챌린지/게시글/댓글 정렬

### Selection Sort

- 구현 위치: `SortUtils.selectionSort`
- 사용 위치:
  - `MealService.sortFoodsByEnergy`
- 용도:
  - 선택 음식 목록을 칼로리 기준으로 정렬

### Counting Sort

- 구현 위치: `SortUtils.countingSort`
- 사용 위치:
  - `MealService.recommendFoods`
- 용도:
  - 목표 칼로리와의 차이(`energyGap`) 기준 추천 정렬

### 실무적 평가

- 학습 목적에는 적합하다.
- 다만 현재 데이터 규모에서는 표준 정렬보다 가독성 이점이 크지 않다.
- `scoreDesc` 정렬처럼 comparator 안에서 비싼 계산을 다시 수행하는 부분은 비효율적이다.

## 10. 뷰 레이어 분석

### 구조

- 공통 조각:
  - `header.jspf`
  - `navbar.jspf`
  - `flash.jspf`
  - `footer.jspf`
- 개별 화면:
  - `auth/login.jsp`
  - `auth/signup.jsp`
  - `home/index.jsp`
  - `meal/list.jsp`
  - `meal/form.jsp`
  - `meal/detail.jsp`
  - `profile/index.jsp`
  - `social/index.jsp`
  - `challenge/index.jsp`
  - `community/index.jsp`
  - `coach/index.jsp`

### 구현 스타일

- JSTL을 추가했지만 실제로는 사용하지 않는다.
- `taglib` 선언이 없다.
- `c:out`, `fmt:*`, `fn:*`도 없다.
- JSP scriptlet/표현식 사용 흔적은 총 389개였다.

즉, 뷰는 EL/JSTL 기반이 아니라 전통적인 scriptlet JSP 스타일이다.

### 스타일링

- Bootstrap CDN 사용
- `assets/css/style.css`에서 카드/그리드/히어로 섹션 중심의 커스텀 스타일 추가
- 디자인 방향은 베이지/그린 계열의 건강/식단 서비스 느낌

## 11. 영속화와 DB 전환 흔적

이 프로젝트는 현재 "메모리 저장소"와 "DB 전환 흔적"이 함께 존재한다.

### 실제 런타임

- `AppContainer`가 `SeedDataFactory`로 저장소를 초기화
- 모든 CRUD는 repository의 메모리 컬렉션에 반영
- 재기동 시 데이터 소실

### DB 관련 흔적

- `pom.xml`에 MySQL 드라이버 의존성 존재
- `DBUtil` 존재

### 하지만 실제 사용은 안 됨

- `DBUtil`은 어떤 서비스/저장소/컨트롤러에서도 호출되지 않는다.
- 즉, 현재 앱은 DB 연결 코드가 있어도 실질적으로는 사용하지 않는다.

### 현재 기준 해석

- 현재 소스 트리에는 `src/main/resources`가 없다.
- 즉, JDBC 의존성과 `DBUtil`은 남아 있지만, DB 스키마/매퍼/DAO까지 연결된 완성형 영속화 레이어는 보이지 않는다.
- 실질적으로는 "DB를 향해 이동하다가, 아직 런타임은 메모리 저장소에 머물러 있는 상태"로 해석하는 편이 맞다.

## 12. 정적 검증 결과

### 확인한 것

- `mvn` 명령은 현재 로컬 환경에 설치되어 있지 않았다.
- 따라서 Maven 기반 전체 빌드/패키징은 이 조사 중 직접 실행하지 못했다.
- 대신 `javac`를 사용해 순수 Java 계층 정적 컴파일을 시도했다.

### javac 결과

- 사용 가능 JDK: `javac 8`
- `model + repository + service + util(일부 제외)` 컴파일:
  - `DBUtil` 제외 시 성공
  - `DBUtil` 포함 시 실패

### DBUtil 실패 원인

- `DBUtil`이 `lombok.NoArgsConstructor`를 사용
- 직접 `javac` 실행 시 Lombok이 classpath에 없어서 실패

### 해석

- 적어도 도메인/서비스/저장소 계층의 순수 Java 코드는 Java 8 문법 수준에서도 대부분 컴파일된다.
- 다만 웹 계층은 `jakarta.servlet` 의존성이 필요하므로 이 조사에서는 완전 컴파일 확인을 못 했다.

## 13. 중요 리스크와 관찰 포인트

### 13.1 높은 우선순위

1. JSP import 패키지 불일치
- `WEB-INF/views`의 10개 파일이 `com.ssafy.yumyum.*`를 import한다.
- 실제 Java 패키지는 `com.ssafy.yumyum.*`다.
- 이 상태라면 JSP 컴파일 시 클래스 해석 실패 가능성이 매우 높다.

2. 보안상 평문 비밀번호 저장
- `User.password`가 평문
- 로그인도 평문 비교
- 시드 데이터도 평문 비밀번호 사용

3. XSS 방어 부재
- 출력 대부분이 `<%= ... %>`로 직접 렌더링된다.
- 게시글 제목/내용, 댓글, 닉네임, 건강 메모, 식단 메모 등 사용자 입력이 HTML escape 없이 출력된다.

4. 문서/설정/코드의 버전 불일치
- README, pom, 코드가 서로 다른 시대의 상태를 동시에 반영하고 있다.
- 신규 참여자가 환경을 맞추기 어렵다.

### 13.2 중간 우선순위

5. CSRF 방어 없음
- 상태 변경은 POST를 쓰지만 CSRF 토큰 개념이 없다.

6. 메모리 저장소와 DB 흔적의 이중 상태
- 지금은 DB를 쓰지 않지만, 의존성과 유틸이 남아 있다.
- 전환 기준이 불명확해 유지보수 비용이 커질 수 있다.

7. 비즈니스 검증 누락
- `SocialService.follow()`는 대상 사용자 존재/활성 여부 미검증
- `CommunityService.createPost()`는 `linkedMealId` 정합성 미검증
- `ChallengeService.joinChallenge()`는 종료된 챌린지 참여 차단 없음

8. 반복 계산에 따른 비효율
- `MealService.sortMeals(... scoreDesc ...)`는 comparator 안에서 `analyzeMeal()`을 반복 호출
- `SocialService.getSuggestions()`와 `getLeaderboard()`는 정렬 비교 중 `countFollowers()`를 반복 호출

9. 작업 트리 오염
- 미추적 `bin/`이 남아 있음
- `.gitignore`에 `bin/`이 없어 Eclipse 산출물이 계속 섞일 가능성 있음

### 13.3 낮은 우선순위지만 참고할 점

10. JSTL 의존성은 있지만 실제 미사용
- 현재는 전통적인 scriptlet JSP 스타일
- 점진적으로 EL/JSTL로 옮기면 가독성이 좋아질 수 있다.

11. 코치 기능은 규칙 기반
- 데모 목적에는 충분하지만, "AI 코치"라는 이름에 비해 구현은 휴리스틱 중심이다.

## 14. 개선 우선순위 제안

### 1차

- JSP import를 전부 `com.ssafy.yumyum.*`로 수정
- README를 현재 실행 기준에 맞게 갱신
- `pom.xml` 기준 Java/Servlet/Tomcat 버전 정책 확정

### 2차

- 비밀번호 해시 도입
- 출력 escape 도입
- CSRF 대응

### 3차

- DB 전환 방향 확정
- `DBUtil`과 repository 구현을 하나의 기준으로 정리
- 현재 도메인 전체를 반영하는 스키마 재설계

### 4차

- JSP scriptlet 축소
- EL/JSTL 또는 템플릿 구조 개선
- 정렬 전 점수/카운트 사전 계산으로 중복 연산 감소

## 15. 최종 평가

이 프로젝트는 학습용 MVC 애플리케이션으로서는 구조가 꽤 잘 보인다. 컨트롤러, 서비스, 저장소, 모델이 명확히 나뉘고, 식단/소셜/챌린지/커뮤니티/코치까지 기능 범위도 넓다. 특히 `MealService`, `CoachService`, `ChallengeService`는 "규칙이 서비스에 모여 있는 전형적인 계층형 설계"를 잘 보여준다.

반면 현재 상태 그대로는 실사용보다 "이행 중인 데모 프로젝트"에 더 가깝다. 가장 큰 이유는 다음 셋이다.

- JSP import 오류 가능성
- 영속화 전략의 미완성
- 보안/문서화의 미정리

따라서 이 저장소의 현재 상태를 한 문장으로 요약하면 다음과 같다.

> 구조는 잘 잡힌 학습형 JSP/Servlet MVC 앱이지만, 실행 환경 정합성과 뷰 패키지 정리, 영속화 방향 통합이 선행되어야 안정적으로 운영 가능한 상태다.
