# dbyum Research Notes

작성일: 2026-05-08
기준 경로: `c:\Users\SSAFY\git\dbyum`

## 현재 기준 정리

현재 이 프로젝트의 기준 DB 스키마는 [SSAFY_COACH_Schema.sql](c:\Users\SSAFY\git\dbyum\src\main\resources\SSAFY_COACH_Schema.sql)이다. 예전 [yumyum_schema.sql](c:\Users\SSAFY\git\dbyum\src\main\resources\yumyum_schema.sql)은 더 이상 기준 문서로 보지 않는 편이 맞다.

이번 정리 기준에서 확인된 핵심은 다음과 같다.

- DB 이름은 `ssafy_yumyumcoach`로 확정되었다.
- `food_nutrition`은 강의 제공 구조를 유지한다.
- `users` 테이블이 추가되어 회원 기능의 영속화 기준이 생겼다.
- `diet_logs`는 `users` FK와 `meal_type`을 포함한다.
- `diet_log_items`는 `selected_grams`를 포함한다.
- 최소 더미 데이터가 함께 들어 있어 단독 실행 테스트가 가능하다.

## 최종 스키마 구조

### 1. users

회원 관리용 테이블이다.

- `user_id`: `INT AUTO_INCREMENT`
- `email`: unique
- `password`, `nickname`, `gender`, `birth_year`
- `height`, `weight`, `goal`, `health_note`
- `active`, `created_at`, `updated_at`

현재 [User.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\model\User.java)는 `id`를 `String`으로 들고 있으므로, JDBC 전환 시에는 repository에서 `INT -> String` 변환이 필요하다.

### 2. food_nutrition

강의 제공 스키마를 유지한다.

- `food_code`
- `food_name`
- `category`
- `weight`
- 영양 컬럼들: `energy_kcal`, `protein_g`, `fat_g`, `carbohydrate_g` 등

`weight`는 문자열 컬럼 그대로 유지되므로, JDBC 매핑 시 `FoodItem`으로 읽어올 때 기준 중량 해석 전략이 필요하다.

현재 [FoodItem.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\model\FoodItem.java)의 `copyWithGrams()`는 100g 기준 계산을 가정하고 있다. 따라서 실제 덤프 데이터의 `weight`가 `100g`가 아닌 값이면 repository에서 별도 환산 규칙을 두거나 모델 계산식을 조정해야 한다.

### 3. diet_logs

식단 헤더 테이블이다.

- `diet_log_id`: `INT AUTO_INCREMENT`
- `user_id`: `users.user_id` FK
- `log_date`
- `meal_type`
- `total_calorie`
- `memo`
- `image_url`
- `created_at`, `updated_at`

현재 [Meal.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\model\Meal.java)는 `id`를 `String`으로 갖고 있으므로, JDBC 전환 시 `diet_log_id`를 문자열로 변환해 매핑하면 read path는 대응 가능하다.

### 4. diet_log_items

식단 상세 항목 테이블이다.

- `diet_log_item_id`: `INT AUTO_INCREMENT`
- `diet_log_id`: `diet_logs.diet_log_id` FK
- `food_code`: `food_nutrition.food_code` FK
- `serving_size`
- `selected_grams`
- `created_at`, `updated_at`

`selected_grams`가 추가되어 현재 UI와 서비스가 다루는 실제 섭취량을 저장할 수 있게 되었다.

## 현재 코드와의 정합성

### 맞아진 점

- `DBUtil`의 DB 이름과 최종 스키마 기준이 모두 `ssafy_yumyumcoach`로 맞는다.
- 식단 기능에 필요한 기본 테이블인 `food_nutrition`, `diet_logs`, `diet_log_items`가 준비되었다.
- 회원 기능용 `users` 테이블이 있어 로그인/프로필 기능을 DB로 옮길 기반이 생겼다.
- 더미 데이터가 포함되어 초기 검증이 쉬워졌다.

### 아직 남아 있는 간극

1. 현재 애플리케이션은 아직 DB를 쓰지 않는다.

[AppContainer.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\util\AppContainer.java)는 여전히 `SeedDataFactory` 기반 in-memory repository를 사용한다.

2. ID 생성 방식이 DB 스키마와 다르다.

[IdGenerator.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\util\IdGenerator.java)는 `user_xxx`, `meal_xxx` 같은 문자열 ID를 생성하지만, 최종 스키마의 `users.user_id`, `diet_logs.diet_log_id`는 `AUTO_INCREMENT INT`다.

즉:

- 조회는 `INT -> String` 매핑으로 대응 가능하다.
- 생성/수정 저장은 JDBC DAO에서 generated key를 받도록 서비스 또는 repository 설계를 바꿔야 한다.

3. 다른 서비스용 테이블은 아직 최종 스키마에 없다.

현재 서비스 계층에는 다음 기능이 있다.

- 소셜
- 챌린지
- 커뮤니티
- 코치

하지만 최종 스키마에는 이들 전용 테이블이 아직 없다. 따라서 이 기능들은 당분간 메모리 저장소에 머물거나, 별도 스키마 확장이 추가로 필요하다.

4. `meal_type` 값 체계를 JDBC에서 통일해야 한다.

[MealService.java](c:\Users\SSAFY\git\dbyum\src\main\java\com\ssafy\yumyum\service\MealService.java)는 `breakfast`, `lunch`, `dinner`, `snack` 같은 영문 내부값을 기준으로 동작한다. 최종 스키마 예시 더미 데이터는 한글 식사명으로도 보일 수 있으므로, 실제 저장값은 하나의 체계로 통일해야 한다.

권장 기준:

- DB 저장값: `breakfast`, `lunch`, `dinner`, `snack`
- 화면 표시: `ViewHelper`에서 한글 라벨 변환

## JDBC 전환 관점의 현재 평가

최종 스키마는 "식단 + 회원" 범위까지는 이전보다 훨씬 명확해졌다. 특히 `food_nutrition`을 유지하면서 `diet_logs`, `diet_log_items`를 그대로 활용하는 방향은 최소 변경 원칙에 맞는다.

다만 지금 상태는 "DB 스키마가 정리된 상태"이지, "코드가 이미 그 스키마와 맞게 동작하는 상태"는 아니다.

현 시점에서 가장 자연스러운 순서는 다음과 같다.

1. `FoodCatalogRepository`를 `food_nutrition` 기반 조회로 전환
2. `MealRepository`를 `diet_logs + diet_log_items` 기반 조회로 전환
3. `UserRepository`를 `users` 기반으로 전환
4. 생성 시 `AUTO_INCREMENT` 키를 받는 방식으로 `save()` 경로 조정
5. 이후 소셜/챌린지/커뮤니티 스키마 확장 여부 결정

## 결론

현재 기준 문서로는 [SSAFY_COACH_Schema.sql](c:\Users\SSAFY\git\dbyum\src\main\resources\SSAFY_COACH_Schema.sql)을 사용하면 된다. 이 파일은 `food_nutrition` 원형을 보존하면서 회원과 식단 저장에 필요한 최소 확장을 반영한 최종본으로 볼 수 있다.

다만 애플리케이션 전체가 이 스키마로 곧바로 전환된 것은 아니다. 지금은 "DB 기준 확정" 단계이며, 실제 JDBC 전환에서는 `AUTO_INCREMENT ID`, `meal_type 저장값`, `weight 해석 규칙`, `비식단 서비스의 영속화 범위`가 다음 핵심 작업이다.
