# Changes

## 기준

- 비교 시작점: `f4732b922a4de2800bf30130840f8c0ccf86622b`
- 비교 대상: 현재 워킹트리 기준
- 포함 범위: 해당 커밋 이후의 모든 커밋 + 아직 커밋되지 않은 SQL 수정 2건

## 한눈에 보기

- 모델 DTO 15개가 수동 getter/setter 방식에서 Lombok `@Data` 기반으로 정리되었다.
- `MealRepository`, `FoodCatalogRepository`, `UserRepository`, `CommunityRepository`가 메모리 기반 저장소에서 JDBC 기반 저장소로 재작성되었다.
- 강의 스펙에 맞춘 SQL 리소스가 추가되었고, 스키마에 `users`, `diet_logs.meal_type`, `diet_log_items.selected_grams`가 반영되었다.
- `SSAFY_COACH_Demo_Dump.sql`이 추가되어 실행 가능한 데모 사용자/식단/식품 데이터 세트가 제공된다.
- 현재 커밋되지 않은 변경으로 `SSAFY_COACH_Schema.sql`, `SSAFY_COACH_Demo_Dump.sql`에 `SET NAMES utf8mb4;`가 추가되었다.

## 상세 정리

### 1. 모델 계층 정리

- `Challenge`, `ChallengeMembership`, `ChallengeParticipant`, `CoachAdvice`, `CommunityComment`, `CommunityPost`, `DailyGoal`, `FollowRelation`, `FoodItem`, `FoodRecommendation`, `Meal`, `MealAnalysis`, `NutritionSummary`, `User`, `WorkoutSession`이 Lombok `@Data`로 전환되었다.
- 그 결과 반복적인 접근자/수정자 코드가 제거되어 모델 클래스가 필드 중심으로 간결해졌다.

### 2. 저장소 계층의 JDBC 전환

- `MealRepository`
  - `Map` 기반 임시 저장소에서 DB 테이블(`meals`, `meal_foods`) 기반 저장소로 변경되었다.
  - 조회 시 식사 헤더와 음식 목록을 조인해서 복원하고, 저장/삭제는 트랜잭션으로 처리한다.
  - seed 데이터가 있을 때 테이블이 비어 있으면 초기 데이터 적재까지 수행한다.

- `FoodCatalogRepository`
  - 메모리 리스트 주입 방식에서 `food_nutrition` 테이블 조회 방식으로 변경되었다.
  - 전체 조회, 코드 조회, 키워드 검색이 SQL로 처리된다.
  - `weight` 문자열에서 g 값을 파싱해 100g 기준 영양값으로 정규화해서 `FoodItem`으로 매핑한다.

- `UserRepository`
  - `users` 테이블을 직접 생성/조회/저장/삭제하는 구조로 바뀌었다.
  - 이메일 조회는 대소문자를 무시하도록 SQL에서 처리한다.
  - `ON DUPLICATE KEY UPDATE` 기반 upsert와 seed 적재 로직이 추가되었다.

- `CommunityRepository`
  - 게시글/댓글을 각각 `community_posts`, `community_comments` 테이블로 관리하도록 변경되었다.
  - 댓글은 게시글 FK에 `ON DELETE CASCADE`가 걸려 있어 게시글 삭제 시 함께 정리된다.
  - 게시글/댓글 모두 upsert, 전체 조회, 단건 조회, 삭제, seed 적재를 지원한다.

- `AppContainer`
  - `FoodCatalogRepository`를 더 이상 `SeedDataFactory.catalogFoods()`로 생성하지 않고, 기본 생성자를 사용해 DB 기반 카탈로그를 읽도록 바뀌었다.

### 3. SQL 스키마와 데이터 리소스 정비

- `src/main/resources/SSAFY_COACH_Schema.sql`
  - 강의 스펙 기반의 신규 스키마 파일이 추가되었다.
  - `users` 테이블이 포함되었고, 회원 관련 기본 컬럼과 활성 여부, 생성/수정 시각이 정의되었다.
  - `diet_logs`에는 `meal_type`이 추가되었다.
  - `diet_log_items`에는 `selected_grams`가 추가되었다.
  - `food_nutrition`은 강의 제공 구조를 유지한 채 사용하도록 정리되었다.
  - 최소 실행용 샘플 `users`, `food_nutrition`, `diet_logs`, `diet_log_items` 데이터가 함께 포함되었다.

- `src/main/resources/SSAFY_COACH_Dump.sql`
  - `food_nutrition` 대용량 데이터 덤프가 추가되었다.

- `src/main/resources/SSAFY_COACH_Demo_Dump.sql`
  - 데모용 사용자 4명, 식품 데이터, 식단 로그, 식단 상세 데이터가 포함된 별도 덤프가 추가되었다.
  - 스키마 적용 후 바로 시연 가능한 수준의 더미 데이터 세트 역할을 한다.

### 4. 문서 정리

- `RESEARCH.md`가 큰 폭으로 갱신되었다.
- `.agent.md`가 정리되었다.
- 기존 `CHANGES.md` 초안이 추가되었고, 이번 작업으로 기준 커밋부터 현재까지의 변경 내용을 다시 요약했다.

## 커밋 흐름 요약

1. `49c8294` 강의자료 SQL 추가
2. `9c8f90e` 모델 DTO에 Lombok 적용
3. `95c0320` `CHANGES.md` 초안 작성
4. `1832b65` 강의 스펙에 맞춘 스키마 재정리
5. `19c5842`, `4725b48`, `715d64f` 문서 업데이트
6. `0672be8` `MealRepository` JDBC 재작성
7. `f43302f` `FoodCatalogRepository` JDBC 재작성
8. `77e45fa` `UserRepository` JDBC 재작성
9. `3aaf398` `CommunityRepository` JDBC 재작성
10. `faddf00` `SSAFY_COACH_Demo_Dump.sql` 추가
11. 현재 미커밋 변경으로 `SSAFY_COACH_Schema.sql`, `SSAFY_COACH_Demo_Dump.sql`에 `SET NAMES utf8mb4;` 추가
