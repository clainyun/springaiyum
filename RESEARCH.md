# dbyum 프로젝트 재조사 보고서

작성일: 2026-05-08  
조사 기준: `c:\Users\SSAFY\git\dbyum` 현재 작업 트리

## 요약

이 프로젝트는 `Jakarta Servlet + JSP` 기반의 전통적인 MVC 웹앱이며, 식단 기록을 중심으로 인증, 프로필, 소셜, 챌린지, 커뮤니티, 규칙 기반 코치 기능을 제공한다.  
코드 구조 자체는 `controller -> service -> repository -> model`로 비교적 명확하지만, 실행 환경과 문서, DB 자산, Eclipse/WTP 설정 사이에 아직 정리되지 않은 지점이 남아 있다.

현재 기준으로 가장 중요한 사실은 다음과 같다.

- 소스 코드의 패키지명은 `com.ssafy.yumyum`으로 일관된다.
- JSP import의 예전 `com.yamyam` 잔재는 현재 `src` 기준으로 제거된 상태다.
- 실제 런타임 데이터는 여전히 `SeedDataFactory` 기반 메모리 저장소를 쓴다.
- `DBUtil`과 `src/main/resources/yumyum_schema.sql`이 다시 존재하지만, 현재 코드 경로에서는 DB를 사용하지 않는다.
- `pom.xml`은 Java 21과 Jakarta 계열 의존성을 선언하지만 `packaging`이 명시되지 않아 Maven 기본값인 `jar`로 해석될 여지가 있다.
- Eclipse 설정은 `Tomcat 11`, Java 21, `jst.web 5.0`인데 `web.xml`은 `6.0`이라 WTP 메타데이터가 완전히 정합하지 않다.
- `src/test` 디렉터리는 생겼지만 실제 테스트 파일은 아직 없다.

## 1. 현재 프로젝트 스냅샷

루트 기준 주요 파일과 디렉터리는 다음과 같다.

- `src/main/java`
- `src/main/resources/yumyum_schema.sql`
- `src/main/webapp`
- `pom.xml`
- `README.md`
- `RESEARCH.md`
- `.classpath`, `.project`, `.settings/`
- `bin/`, `target/`

관찰 포인트:

- `git status --short` 기준 작업 트리는 현재 깨끗했다.
- 다만 프로젝트 루트에는 Eclipse 관련 메타데이터와 산출물 디렉터리가 함께 존재한다.
- `.gitignore`에는 `bin/`, `.settings/`, `.classpath`, `.project` 등이 들어 있어 로컬 IDE 중심 운영 흔적이 뚜렷하다.

## 2. 기술 스택과 실행 환경

### 실제 코드 기준

- 언어: Java
- 웹 기술: Jakarta Servlet, JSP
- UI: Bootstrap 5, Bootstrap Icons, 커스텀 CSS
- 빌드 도구: Maven
- 데이터 저장: In-memory repository
- DB 흔적: MySQL JDBC, DBUtil, SQL 스키마 파일

### `pom.xml` 기준

현재 `pom.xml`은 다음을 선언한다.

- `groupId`: `com.ssafy`
- `artifactId`: `dbyum`
- `version`: `0.0.1-SNAPSHOT`
- `maven.compiler.release`: `21`
- 의존성:
  - `org.projectlombok:lombok`
  - `jakarta.servlet:jakarta.servlet-api:6.1.0`
  - `jakarta.servlet.jsp:jakarta.servlet.jsp-api:4.0.0`
  - `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.2`
  - `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`
  - `com.mysql:mysql-connector-j:8.3.0`

### 설정상 주의점

- `packaging`이 없다. Maven 기본 packaging은 `jar`이므로, 웹앱이라면 `war`를 명시하는 편이 안전하다.
- `maven-war-plugin` 설정도 없다.
- 즉, Eclipse WTP로는 돌아갈 수 있어도 Maven 중심 표준 웹 패키징 관점에서는 미완성이다.

## 3. Eclipse / WTP 설정 상태

이번 재조사에서 코드보다 더 많이 바뀌었거나 중요한 부분은 IDE 설정이었다.

### `.classpath`

- `src/main/java`, `src/main/resources`
- `src/test/java`, `src/test/resources`
- `target/generated-sources/annotations`
- `target/generated-test-sources/test-annotations`
- JRE: `JavaSE-21`
- 서버 런타임: `Apache Tomcat v11.0`

### `org.eclipse.wst.common.component`

배포 어셈블리에는 다음이 포함된다.

- `/src/main/webapp`
- `/src/main/java` -> `/WEB-INF/classes`
- `/src/main/resources` -> `/WEB-INF/classes`
- `/src/test/java` -> `/WEB-INF/classes`
- `/src/test/resources` -> `/WEB-INF/classes`
- 생성 소스 디렉터리

### 문제점

- 테스트 소스가 웹 배포물에 포함되도록 잡혀 있다.
- 지금은 테스트 파일이 비어 있어 영향이 작지만, 일반적으로는 잘못된 배포 설정이다.
- `web.xml`은 6.0인데 facet은 `jst.web 5.0`이다.

### `web.xml` vs facet

- `web.xml`: Jakarta Web App `version="6.0"`
- WTP facet: `jst.web version="5.0"`

이 조합은 Eclipse의 JSP validator나 서버 어댑터에서 미묘한 경고를 만들 수 있다.

## 4. README와 현재 코드의 불일치

README는 여전히 예전 상태를 설명하고 있다.

확인된 차이:

- README는 `Java 8` 기준
- 현재 `pom.xml`은 Java 21 기준
- README는 `Tomcat 9 이상` 기준
- 현재 코드는 `jakarta.servlet.*`를 사용하므로 Tomcat 10.1+/11 계열이 더 자연스럽다
- README는 `com/yamyam` 패키지 경로를 설명
- 실제 코드는 `com/ssafy/yumyum`
- README의 예시 URL은 `yamyam-mvc-1.0.0`
- 현재 프로젝트 식별자는 `dbyum`

즉, README는 현행 코드베이스 설명서로 보기 어렵다.

## 5. 소스 구조 요약

현재 기준 파일 수는 다음과 같다.

- controller: 8개
- service: 7개
- repository: 6개
- model: 15개
- view(JSP/JSPF): 15개

`src/test`는 존재하지만 실제 테스트 파일은 없다.

## 6. 런타임 구조

### 전반 구조

프로젝트는 다음 흐름을 따른다.

1. `@WebServlet` 컨트롤러가 요청 수신
2. 서비스 계층이 비즈니스 로직 수행
3. 저장소 계층이 메모리 컬렉션을 조작
4. JSP가 최종 렌더링

### DI 방식

`AppContainer`가 정적 싱글턴으로 다음을 보관한다.

- Repository 인스턴스
- Service 인스턴스
- SeedDataFactory 기반 초기 데이터

스프링 없이 수동으로 구성된 서비스 로케이터 패턴에 가깝다.

### 공통 컨트롤러 기반

`BaseController`는 다음 책임을 맡는다.

- 요청/응답 UTF-8 처리
- flash message 노출
- JSP forward 렌더링
- 로그인 사용자 강제

## 7. 기능 지도

### 인증

- 로그인
- 회원가입
- 로그아웃

특징:

- 비밀번호는 평문 비교
- 활성 사용자만 로그인 허용

### 홈

- 최근 식단
- 오늘 영양 요약
- 일일 목표
- 코치 요약
- 소셜 수치
- 활성 챌린지 일부

### 식단

- 목록, 상세, 등록, 수정, 삭제
- 날짜/식사유형 필터
- 정렬 기준 선택
- 영양 분석
- 음식 검색
- 음식 추천

### 프로필

- 사용자 정보 수정
- 목표/신체 정보 수정
- 계정 비활성화
- 계정 삭제

### 소셜

- 팔로우/언팔로우
- 팔로워/팔로잉 조회
- 추천 사용자
- 팔로워 수 기반 리더보드

### 챌린지

- 생성
- 참여
- 탈퇴
- 진행률 갱신
- 생성자 삭제

### 커뮤니티

- 게시글 CRUD
- 댓글 CRUD
- 카테고리 필터
- 식단 연결

### 코치

- 최근 식단 분석 요약
- 영양 상태에 따른 문구 생성
- 목표별 운동 세션 제안
- 챌린지 연계 next action

중요한 점: 이 기능은 외부 AI API가 아니라 규칙 기반 생성 로직이다.

## 8. 데이터 저장 방식

### 실제 사용 중인 저장 방식

현재 앱은 DB가 아니라 메모리 저장소를 사용한다.

- `UserRepository`
- `MealRepository`
- `FoodCatalogRepository`
- `SocialRepository`
- `ChallengeRepository`
- `CommunityRepository`

모두 `Map`이나 `List` 중심이며, 서버 재시작 시 데이터가 초기화된다.

### 초기 데이터

`SeedDataFactory`가 다음 데이터를 만든다.

- 사용자
- 음식 카탈로그
- 식단
- 팔로우 관계
- 챌린지
- 챌린지 멤버십
- 커뮤니티 게시글/댓글

즉, 현재 실제 동작 범위는 식단만이 아니라 소셜/챌린지/커뮤니티까지 포함한 메모리 시드 기반이다.

## 9. DB 관련 자산의 현재 의미

현재 코드베이스에는 DB 자산이 다시 존재한다.

### 존재하는 것

- `DBUtil`
- MySQL JDBC 의존성
- `src/main/resources/yumyum_schema.sql`

### 하지만 실제로는 사용 안 함

`DBUtil`은 검색 결과 기준으로 어떤 서비스/저장소/컨트롤러에서도 호출되지 않는다.  
즉, 현재 앱은 실질적으로 DB 무관 상태다.

### 내부 불일치

- `DBUtil` 접속 DB 이름: `ssafy_yumyumcoach`
- SQL 파일 생성 DB 이름: `yamyam_db`

이 둘은 서로 맞지 않는다.

### SQL 스키마 범위

`yumyum_schema.sql`이 다루는 테이블은 다음뿐이다.

- `users`
- `foods`
- `meals`
- `meal_foods`

즉, 현재 앱이 제공하는 전체 기능 중 다음은 SQL 스키마에 반영되지 않았다.

- 소셜
- 챌린지
- 커뮤니티
- 코치

### SQL 파일 자체의 품질 문제

시드 INSERT 구문에 깨진 문자열과 닫히지 않은 따옴표처럼 보이는 부분이 존재한다.  
따라서 이 SQL은 현재 상태 그대로는 실제 DB에 바로 적용되지 않을 가능성이 높다.

즉, DB 전환 자산은 "다시 생기긴 했지만 아직 신뢰 가능한 배포 자산은 아님"이 현재 평가다.

## 10. JSP 상태

### 좋아진 점

- `com.yamyam` import 잔재가 현재 `src` 기준으로 제거됐다.
- JSP import는 이제 `com.ssafy.yumyum.*`를 참조한다.

### 여전히 남아 있는 점

- JSTL 의존성은 있지만 실제 `taglib` 사용은 거의 없다.
- 뷰는 여전히 scriptlet 기반이다.
- 사용자 입력이 `<%= ... %>`로 직접 출력되는 구간이 많다.

이 구조는 학습용으로는 단순하지만, 유지보수성과 보안 면에서는 약하다.

## 11. 알고리즘 사용 위치

현재도 알고리즘 설명은 유효하다.

- `SortUtils.quickSort`
  - 식단 정렬
  - 팔로워/팔로잉 정렬
  - 게시글/댓글/챌린지 정렬
- `SortUtils.selectionSort`
  - 선택 음식 목록 정렬
- `SortUtils.countingSort`
  - 추천 음식 정렬

학습 목적에는 맞지만, 일부 비교 로직은 계산 비용이 반복된다.

예:

- `MealService.sortMeals(... scoreDesc ...)`는 comparator 내부에서 `analyzeMeal()`을 반복 호출
- `SocialService`는 정렬 비교 중 팔로워 수를 반복 계산

## 12. 검증 결과

### 로컬 도구 상태

- `mvn`: 없음
- `mvnw.cmd`: 없음
- `javac`: 있음
- 로컬 javac 버전: `1.8.0_192`

### 해석

프로젝트는 Java 21을 요구하지만, 현재 터미널에서 바로 사용할 수 있는 `javac`는 Java 8이다.  
즉, Maven/Java 21 기준의 전체 빌드를 이 환경에서 직접 재현하기는 어렵다.

### 수행한 정적 검증

다음 범위는 `javac`로 별도 컴파일 확인했다.

- `model`
- `repository`
- `service`
- `util` 일부

제외:

- `BaseController`
- `SessionUtils`
- `DBUtil`

결과:

- 위 제외 범위는 현재도 정적 컴파일 성공
- `DBUtil`은 Lombok 의존성 때문에 단독 javac 검증 대상에서 제외
- 웹 계층은 Jakarta Servlet API classpath가 필요해 이 조사에서는 완전 검증하지 못함

## 13. 현재 시점의 핵심 문제

### 1. Maven 웹 패키징 정합성 부족

- `pom.xml`에 `packaging=war`가 없다
- war plugin 설정도 없다

Eclipse WTP와 Maven 관점이 분리돼 있다.

### 2. WTP 설정 불일치

- `web.xml`은 6.0
- facet은 5.0
- test source가 배포 어셈블리에 포함

즉, IDE 실행은 될 수 있어도 설정 일관성이 약하다.

### 3. README가 현재 코드와 맞지 않음

- Java 버전
- Tomcat 버전
- 패키지 경로
- 예시 URL

모두 최신 상태와 어긋난다.

### 4. DB 자산이 반쯤만 복구됨

- 스키마 파일은 다시 생김
- 하지만 실제 런타임은 메모리 저장소
- DBUtil DB명과 SQL DB명이 다름
- SQL 시드 품질도 불안정

### 5. 보안 리스크 지속

- 평문 비밀번호 저장/비교
- XSS escape 부재
- CSRF 방어 없음

### 6. 테스트 부재

- `src/test`는 존재
- 실제 테스트 파일은 없음

즉, 테스트 구조만 만들어지고 검증 코드가 아직 없다.

## 14. 이전 조사 대비 달라진 점

이번 재조사에서 달라진 핵심은 다음이다.

- `src/main/resources/yumyum_schema.sql`이 다시 존재한다
- `src/test/java`, `src/test/resources` 디렉터리가 생겼다
- `.gitignore`에 `bin/` 등 Eclipse 산출물 무시 규칙이 들어갔다
- JSP import의 `com.yamyam` 잔재는 현재 소스 기준으로 정리됐다
- 반대로 `pom.xml`은 이전에 보였던 WAR/빌드 플러그인 명시가 사라지고 더 단순한 상태다

즉, 코드 로직이 크게 바뀌었다기보다 "설정과 보조 자산이 다시 흔들리는 중"에 가깝다.

## 15. 권장 정리 순서

1. `pom.xml`에 `packaging>war</packaging>`와 WAR 빌드 기준을 확정한다.
2. Eclipse facet을 `web.xml`과 맞는 버전으로 정리한다.
3. Deployment Assembly에서 `src/test/*`를 웹 배포 대상에서 제거한다.
4. README를 현재 실행 기준으로 전면 갱신한다.
5. DB를 쓸지, 메모리 저장소를 유지할지 방향을 먼저 결정한다.
6. DB를 쓴다면 `DBUtil`, schema, repository를 한 기준으로 재설계한다.
7. 비밀번호 해시, 출력 escape, CSRF 대응을 넣는다.
8. 최소한 서비스 계층 테스트부터 추가한다.

## 최종 판단

현재 프로젝트는 여전히 "학습용 MVC 앱으로서의 구조"는 분명하고, 코드 읽기 난이도도 과하게 높지 않다.  
하지만 최근 변경은 기능 추가보다 설정 재편과 자산 복구에 더 가까워서, 지금 가장 중요한 일은 새 기능보다 **실행 기준 정합성 확정**이다.

한 문장으로 요약하면 다음과 같다.

> 지금 코드베이스는 기능 구조보다 설정 계층이 더 많이 흔들리고 있으며, 우선 Maven/WTP/DB 자산의 기준을 하나로 맞추는 정리가 필요하다.
