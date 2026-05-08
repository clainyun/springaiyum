# YamYam MVC Project

`yamyam_project.pdf` 요구사항에 맞춰 기존 정적 HTML 버전을 `JSP + Servlet + MVC` 구조로 전환한 프로젝트입니다.

현재 프로젝트의 핵심 방향은 다음과 같습니다.

- 화면을 `JSP` 파일 구조로 구성
- `Controller -> Service -> Repository` 계층 분리
- 식단, 회원, 소셜, 챌린지, 커뮤니티, AI 코치 기능을 서버 렌더링 기반으로 재구성
- 정렬/추천 알고리즘을 `Service` 계층에 반영

## 기술 스택

- Java 8
- JSP / Servlet
- Bootstrap 5
- Maven WAR 구조
- In-Memory Repository

## 실행에 필요한 핵심 경로

```text
src/main/java/com/yamyam/
src/main/webapp/
pom.xml
README.md
docs/
```

## 프로젝트 구조

```text
yamyam/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/com/yamyam/
│  │  │  ├─ controller/
│  │  │  ├─ service/
│  │  │  ├─ repository/
│  │  │  ├─ model/
│  │  │  └─ util/
│  │  └─ webapp/
│  │     ├─ index.jsp
│  │     ├─ assets/css/style.css
│  │     └─ WEB-INF/
│  │        ├─ web.xml
│  │        └─ views/
│  │           ├─ common/
│  │           ├─ auth/
│  │           ├─ home/
│  │           ├─ meal/
│  │           ├─ profile/
│  │           ├─ social/
│  │           ├─ challenge/
│  │           ├─ community/
│  │           └─ coach/
├─ docs/
│  ├─ architecture-diagram.md
│  └─ class-diagram.md
├─ yamyam_project.pdf
└─ yamyam_참고.pdf
```

## MVC 계층 구성

### Controller

- `AuthController`
- `HomeController`
- `MealController`
- `ProfileController`
- `SocialController`
- `ChallengeController`
- `CommunityController`
- `CoachController`

### Service

- `AuthService`
- `UserService`
- `MealService`
- `SocialService`
- `ChallengeService`
- `CommunityService`
- `CoachService`

### Repository

- `UserRepository`
- `MealRepository`
- `FoodCatalogRepository`
- `SocialRepository`
- `ChallengeRepository`
- `CommunityRepository`

## JSP 화면 구성

- `auth/login.jsp`: 로그인
- `auth/signup.jsp`: 회원가입
- `home/index.jsp`: 대시보드
- `meal/list.jsp`: 식단 목록
- `meal/form.jsp`: 식단 등록/수정
- `meal/detail.jsp`: 식단 상세
- `profile/index.jsp`: 회원 정보 조회/수정, 비활성화/삭제
- `social/index.jsp`: 팔로우/언팔로우/추천/리더보드
- `challenge/index.jsp`: 챌린지 생성/참여/진행률 관리
- `community/index.jsp`: 게시글/댓글 CRUD
- `coach/index.jsp`: AI 식단/운동 코칭

## 요구사항 반영 범위

### 필수 기능

- `F101` 음식 DB 기반 식단 생성
- `F102` 식단 상세 조회
- `F103` 식단 수정
- `F104` 식단 삭제
- `F105` 식단 영양 분석
- `F106` 회원가입
- `F107` 회원 정보 조회
- `F108` 회원 정보 수정
- `F109` 계정 비활성화 / 삭제
- `F110` 로그인 / 로그아웃

### 추가 기능

- `F111` 팔로우 추가 / 삭제 / 목록 관리
- `F112` 챌린지 정보 관리
- `F113` 챌린지 참여 및 진행률 관리
- `F114` 커뮤니티 게시글 CRUD
- `F115` 댓글 CRUD
- `F116` AI 스타일 식단 분석
- `F117` AI 스타일 운동 코칭

## 알고리즘 적용

- `Quick Sort`
  - 위치: `MealService.sortMeals`
  - 용도: 식단 목록 정렬

- `Selection Sort`
  - 위치: `MealService.sortFoodsByEnergy`
  - 용도: 선택 음식 칼로리 내림차순 정렬

- `Counting Sort`
  - 위치: `MealService.recommendFoods`
  - 용도: 목표 칼로리와 차이가 적은 음식 추천

## 실행 방법

이 프로젝트는 `JSP/Servlet` 기반이라 `Tomcat` 같은 서블릿 컨테이너에서 실행해야 합니다.

### 방법 1. IntelliJ IDEA에서 실행

1. 프로젝트 폴더를 IntelliJ로 엽니다.
2. `Project SDK`를 `Java 8`로 맞춥니다.
3. `Tomcat 9 이상`을 설치하고 IntelliJ에 등록합니다.
4. `Run/Debug Configurations`에서 `Tomcat Server > Local`을 추가합니다.
5. `Deployment`에 이 프로젝트를 `war exploded` 형태로 추가합니다.
6. 서버를 실행한 뒤 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8080/yamyam-mvc-1.0.0/home
```

배포 이름이 다르면 마지막 경로 일부는 달라질 수 있습니다.

### 방법 2. Eclipse에서 실행

1. Eclipse에서 프로젝트를 `Existing Maven Project` 또는 `Dynamic Web Project`로 import 합니다.
2. `Tomcat Runtime`을 등록합니다.
3. 프로젝트를 서버에 추가합니다.
4. 실행 후 `/home` 경로로 접속합니다.

### 방법 3. Maven 빌드 후 Tomcat 배포

Maven이 설치되어 있다면:

```bash
mvn clean package
```

빌드가 끝나면 생성된 `war` 파일을 Tomcat `webapps` 폴더에 넣고 서버를 실행합니다.

## 데모 계정

- 이메일: `demo@yamyam.com`
- 비밀번호: `Demo1234!`

## 시드 데이터

초기 구동 시 메모리 저장소에 다음 데이터가 올라갑니다.

- 사용자 4명
- 식단 기록 4건
- 팔로우 관계 3건
- 챌린지 3건
- 챌린지 참여 3건
- 게시글 3건
- 댓글 2건
- 음식 카탈로그 15건

## 다이어그램 문서

- 아키텍처 초안: [docs/architecture-diagram.md](./docs/architecture-diagram.md)
- 클래스 다이어그램 초안: [docs/class-diagram.md](./docs/class-diagram.md)

## 검증 상태

확인 완료:

- `model / repository / service / util(AppContainer 포함)` Java 계층 `javac` 컴파일 통과
- JSP 뷰 파일 및 경로 구성 완료

아직 이 환경에서 미검증:

- `Servlet API` 포함 상태에서 `controller` 계층 실제 컴파일
- `Tomcat` 실제 기동 및 JSP 렌더링

즉, 현재 결과물은 `JSP + MVC 구조로 전환된 실행용 초안 프로젝트`이며, 마지막 단계는 로컬 Tomcat 배포 확인입니다.
