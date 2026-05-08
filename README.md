# YumYumCoach DB PJT

- 1541268 윤다인
- 1544345 김동주

## 변경 개요

기존 Servlet/JSP 기반 냠냠코치 프로젝트의 저장 방식을 메모리 기반에서 JDBC 기반 MySQL 연동 방식으로 변경했습니다.

---

## AS-IS

- `Repository`가 `Map`, `List` 기반으로 데이터를 관리했습니다.
- 서버 재시작 시 회원, 식단, 음식 데이터가 유지되지 않았습니다.
- 음식, 식단, 회원 데이터가 실제 DB와 연결되지 않았습니다.
- 강의 제공 음식 영양정보 테이블을 프로젝트 기능에 직접 활용하지 않았습니다.

---

## TO-BE

- `DBUtil`을 통해 MySQL Connection을 공통으로 생성하도록 구성했습니다.
- `UserRepository`, `MealRepository`, `FoodCatalogRepository`를 JDBC 기반으로 수정했습니다.
- 회원 정보는 `users` 테이블에서 관리합니다.
- 식단 기록은 `diet_logs`, 식단 상세 음식은 `diet_log_items`에서 관리합니다.
- 음식 영양정보는 강의 제공 스펙의 `food_nutrition` 테이블을 사용합니다.
- `PreparedStatement`를 사용해 SQL을 실행하고, 조회 결과는 모델 객체로 매핑했습니다.

---

## JDBC 적용 기능

| 기능          | 적용 테이블               | SQL                      |
| ------------- | ------------------------- | ------------------------ |
| 회원가입      | users                     | INSERT                   |
| 로그인        | users                     | SELECT                   |
| 프로필 조회   | users                     | SELECT                   |
| 프로필 수정   | users                     | UPDATE                   |
| 계정 비활성화 | users                     | UPDATE                   |
| 계정 삭제     | users                     | DELETE                   |
| 식단 조회     | diet_logs, diet_log_items | SELECT                   |
| 식단 등록     | diet_logs, diet_log_items | INSERT                   |
| 식단 수정     | diet_logs, diet_log_items | UPDATE / DELETE / INSERT |
| 식단 삭제     | diet_logs                 | DELETE                   |
| 음식 조회     | food_nutrition            | SELECT                   |

---

## 스키마 보완 사항

- 회원 기능 구현을 위해 `users` 테이블을 추가했습니다.
- 사용자별 식단 관리를 위해 `diet_logs.user_id`를 `users.user_id`와 연결했습니다.
- 식사 유형 구분을 위해 `diet_logs.meal_type`을 추가했습니다.
- 섭취량 저장을 위해 `diet_log_items.selected_grams`를 추가했습니다.
- 한글 데이터 처리를 위해 SQL 파일에 `SET NAMES utf8mb4;`를 반영했습니다.

---

## 테스트 계정

```text
demo@yumyam.com
Demo1234!
```

## ERD 와 MySQL Workbench 파일

- [./assets/ssafy_yumyumcoach.mwb](./assets/ssafy_yumyumcoach.mwb)
- [./assets/ssafy_yumyumcoach.png](./assets/ssafy_yumyumcoach.png)
- [./assets/ssafy_yumyumcoach.sql](./assets/ssafy_yumyumcoach.sql)

### ERD 미리 보기

![](./assets/ssafy_yumyumcoach.png)
