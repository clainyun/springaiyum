# Batch Brainstorming

이 문서는 `./data` 폴더에 수집된 음식/칼로리 데이터를 바탕으로, YumYumCoach 프로젝트에서 연습해볼 수 있는 Spring Batch 처리 아이디어를 정리한다.

현재 데이터는 크게 두 종류다.

| 데이터 | 파일 | 성격 |
| --- | --- | --- |
| 농림수산식품교육문화정보원 칼로리 정보 | `data/농림수산식품교육문화정보원_칼로리 정보_20190926/농림수산식품교육문화정보원_칼로리 정보_20190926.csv` | 음식명, 1인분 칼로리, 탄수화물, 단백질, 지방, 콜레스테롤, 식이섬유, 나트륨, 등록일 중심의 정량 영양 데이터 |
| 음식별 칼로리/지역 음식 정보 | `data/충청남도교육청_충남방과후학교지원센터_음식별칼로리/전북특별자치도_음식정보_20191219.csv` | 지역 음식명, 설명, 재료, 조리 이야기, 참고 정보 등 텍스트가 긴 음식 콘텐츠 데이터 |

두 CSV 모두 한 번에 읽어도 되는 크기지만, 배치 처리 연습을 위해 일부러 page/chunk 단위로 쪼개 읽고, staging 테이블에 먼저 적재한 뒤 정제/검증/최종 반영/리포트 단계를 나누는 방식이 적합하다.

## 공통 배치 연습 방향

모든 아이디어에서 공통으로 적용할 수 있는 배치 패턴은 다음과 같다.

1. `FlatFileItemReader` 또는 직접 구현한 paging reader로 CSV를 일정 건수씩 읽는다.
2. 원본 문자열을 그대로 staging 테이블에 저장한다.
3. Processor에서 숫자 변환, 공백 제거, 단위 정규화, 음식명 표준화를 수행한다.
4. 정제 성공 데이터는 final 테이블에 upsert한다.
5. 정제 실패 데이터는 reject/error 테이블에 저장한다.
6. 마지막 Tasklet에서 처리 건수, 성공/실패 건수, 중복 건수, 결측치 건수를 요약한다.

배치 연습용 chunk 크기는 실제 데이터 크기보다 작게 잡는 것이 좋다.

| 목적 | 권장 chunk/page 크기 |
| --- | --- |
| 처리 흐름 확인 | 10건 |
| 실패/재시작 연습 | 25건 |
| 약간 현실적인 적재 | 100건 |

## 아이디어 1. 음식 영양 카탈로그 정제/적재 배치

### 목표

농림수산식품교육문화정보원의 칼로리 CSV를 현재 프로젝트의 `food_nutrition` 테이블에 바로 쓰기 좋은 형태로 정제한다.

현재 앱의 식단 등록 기능은 `food_nutrition` 테이블을 음식 검색과 영양 계산의 기준으로 사용한다. 따라서 이 배치는 서비스 품질에 바로 연결된다.

### 입력 데이터

`농림수산식품교육문화정보원_칼로리 정보_20190926.csv`

주요 컬럼은 다음처럼 해석할 수 있다.

- 음식명
- 1인분 칼로리(kcal)
- 탄수화물(g)
- 단백질(g)
- 지방(g)
- 콜레스테롤(g)
- 식이섬유(g)
- 나트륨(g)
- 등록일

### 배치 흐름

1. CSV를 10건 또는 50건 단위로 읽는다.
2. 원본 행을 `food_nutrition_staging`에 저장한다.
3. 음식명 trim, 괄호/특수문자 정리, 숫자 컬럼 파싱을 수행한다.
4. 칼로리/탄수화물/단백질/지방 중 필수값이 없거나 숫자로 변환할 수 없으면 reject 처리한다.
5. 정상 데이터는 `food_nutrition`에 upsert한다.
6. 적재 완료 후 `food_import_report`에 처리 결과를 저장한다.

### 테이블 설계안

```sql
CREATE TABLE food_nutrition_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_file VARCHAR(255) NOT NULL,
    row_no INT NOT NULL,
    raw_food_name VARCHAR(255),
    raw_calories VARCHAR(50),
    raw_carbs VARCHAR(50),
    raw_protein VARCHAR(50),
    raw_fat VARCHAR(50),
    raw_cholesterol VARCHAR(50),
    raw_fiber VARCHAR(50),
    raw_sodium VARCHAR(50),
    raw_registered_date VARCHAR(50),
    import_status VARCHAR(20) DEFAULT 'READY',
    error_message VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_food_staging_source_row (source_file, row_no)
);

CREATE TABLE food_nutrition (
    food_code VARCHAR(64) PRIMARY KEY,
    food_name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    weight VARCHAR(50) DEFAULT '100g',
    energy_kcal DOUBLE NOT NULL,
    carbohydrate_g DOUBLE NOT NULL,
    protein_g DOUBLE NOT NULL,
    fat_g DOUBLE NOT NULL,
    cholesterol_g DOUBLE,
    fiber_g DOUBLE,
    sodium_g DOUBLE,
    source_name VARCHAR(100),
    source_row_no INT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_food_nutrition_name_source (food_name, source_name)
);

CREATE TABLE food_import_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT,
    source_file VARCHAR(255),
    total_count INT,
    success_count INT,
    reject_count INT,
    duplicate_count INT,
    started_at DATETIME,
    finished_at DATETIME
);
```

### 연습 포인트

- `FlatFileItemReader`로 CSV 읽기
- `@StepScope`로 `sourceFile`, `chunkSize` JobParameter 받기
- Processor에서 숫자 파싱 실패 시 skip/reject 분리
- `ON DUPLICATE KEY UPDATE` 기반 재실행 가능 upsert
- 같은 파일을 두 번 돌렸을 때 중복 삽입 없이 갱신되는지 확인

### 현재 프로젝트에 붙였을 때 효과

음식 검색 품질이 좋아지고, 식단 등록 시 선택 가능한 음식 데이터가 늘어난다. 사용자는 공공 데이터 기반 영양값으로 식단을 기록할 수 있다.

## 아이디어 2. 음식명 표준화와 중복 병합 배치

### 목표

두 데이터셋을 함께 사용하면 같은 음식이 서로 다른 이름으로 들어올 가능성이 높다. 예를 들어 괄호, 조리법, 지역명, 공백 차이 때문에 같은 음식이 중복 음식처럼 보일 수 있다.

이 배치는 원본 음식명을 표준화하고, 같은 음식으로 보이는 데이터를 대표 음식으로 묶는 연습용 배치다.

### 입력 데이터

- 정량 영양 데이터 CSV
- 지역 음식 정보 CSV
- 이미 적재된 `food_nutrition`
- 지역 음식 staging 테이블

### 배치 흐름

1. 두 CSV를 각각 staging 테이블에 적재한다.
2. 음식명에서 괄호 내용, 중복 공백, 특수문자, 지역 접두어 등을 제거해 `normalized_name`을 만든다.
3. `normalized_name` 기준으로 후보를 그룹화한다.
4. 완전 일치 데이터는 자동 병합 후보로 표시한다.
5. 부분 일치 데이터는 검수 후보로 `food_merge_candidate`에 저장한다.
6. 병합 결과를 `food_master`에 반영한다.

### 테이블 설계안

```sql
CREATE TABLE food_master (
    food_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    canonical_name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    representative_food_code VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_food_master_normalized (normalized_name)
);

CREATE TABLE food_alias (
    alias_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    alias_name VARCHAR(200) NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    source_row_no INT,
    confidence_score INT DEFAULT 100,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_food_alias (alias_name, source_name),
    INDEX idx_food_alias_food_id (food_id)
);

CREATE TABLE food_merge_candidate (
    candidate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    left_source VARCHAR(100),
    left_name VARCHAR(200),
    right_source VARCHAR(100),
    right_name VARCHAR(200),
    normalized_name VARCHAR(200),
    match_type VARCHAR(30),
    confidence_score INT,
    review_status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 연습 포인트

- 여러 source를 순차 Step으로 처리하기
- Processor에서 문자열 정규화
- 중복 데이터 탐지
- 자동 처리 데이터와 검수 필요 데이터를 분리
- Tasklet으로 병합 후보 리포트 생성

### 현재 프로젝트에 붙였을 때 효과

사용자가 음식 검색을 할 때 같은 음식이 여러 이름으로 흩어지는 문제를 줄일 수 있다. 이후 식단 기록에서 `food_code` 대신 `food_id` 중심으로 추천/분석을 개선할 수 있다.

## 아이디어 3. 지역 음식 콘텐츠 적재와 태그 추출 배치

### 목표

전북 지역 음식 정보 CSV는 칼로리보다 음식 설명, 재료, 조리 이야기 같은 텍스트 콘텐츠가 풍부하다. 이 데이터를 앱의 음식 상세 설명, 커뮤니티 글감, 추천 태그에 활용할 수 있도록 정제한다.

### 입력 데이터

`전북특별자치도_음식정보_20191219.csv`

주요 성격은 다음과 같다.

- 연번
- 음식이름
- 음식내용
- 음식재료
- 음식이야기/자막 같은 긴 설명
- 지역 정보
- 참고 정보

### 배치 흐름

1. CSV를 5건 또는 10건 단위로 읽는다. 한 행의 텍스트가 길기 때문에 chunk를 작게 잡는다.
2. 원본 행 전체를 `regional_food_staging`에 저장한다.
3. 음식명, 지역명, 재료 문자열, 설명 문자열을 정제한다.
4. 재료 문자열을 콤마/공백/단위 기준으로 나눠 `regional_food_ingredient`에 저장한다.
5. 음식 설명에서 너무 긴 텍스트는 요약용 컬럼과 원문 컬럼으로 분리한다.
6. 태그 후보를 추출해 `food_tag`에 저장한다.

### 테이블 설계안

```sql
CREATE TABLE regional_food (
    regional_food_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_row_no INT NOT NULL,
    food_name VARCHAR(200) NOT NULL,
    region_name VARCHAR(100),
    summary VARCHAR(1000),
    story_text MEDIUMTEXT,
    reference_text VARCHAR(1000),
    source_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_regional_food_source_row (source_name, source_row_no)
);

CREATE TABLE regional_food_ingredient (
    ingredient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    regional_food_id BIGINT NOT NULL,
    ingredient_name VARCHAR(100) NOT NULL,
    raw_text VARCHAR(300),
    display_order INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_regional_ingredient_food (regional_food_id),
    INDEX idx_regional_ingredient_name (ingredient_name)
);

CREATE TABLE food_tag (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(30),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_food_tag (target_type, target_id, tag_name)
);
```

### 연습 포인트

- 긴 텍스트 CSV 필드 처리
- 작은 chunk로 안정적으로 커밋하기
- 하나의 입력 row에서 master row 1개와 child row N개 생성하기
- Writer를 두 개 이상 쓰는 구조 연습
- 원문 보존 테이블과 서비스용 정제 테이블 분리

### 현재 프로젝트에 붙였을 때 효과

음식 검색 결과에 단순 영양값뿐 아니라 음식 설명, 지역성, 대표 재료를 함께 보여줄 수 있다. 커뮤니티에서 식단 리뷰 글을 작성할 때 연결할 음식 설명도 풍부해진다.

## 아이디어 4. 영양 품질 검증과 이상치 리포트 배치

### 목표

공공 CSV는 빈 값, 이상한 단위, 너무 큰 값, 등록일 누락 같은 품질 이슈가 있을 수 있다. 이 배치는 음식 영양 데이터의 품질을 검증하고, 관리자가 볼 수 있는 리포트를 생성한다.

### 입력 데이터

- `food_nutrition_staging`
- `food_nutrition`

### 검증 규칙 예시

| 규칙 | 예시 처리 |
| --- | --- |
| 칼로리 음수 또는 0 | reject 또는 warning |
| 탄수화물/단백질/지방 음수 | reject |
| 칼로리가 1,500kcal 초과 | warning |
| 나트륨이 비정상적으로 큼 | warning |
| 음식명 누락 | reject |
| 등록일 형식 오류 | warning |
| 동일 음식명 중 영양값 차이가 큼 | duplicate_conflict |

### 배치 흐름

1. staging 또는 final 테이블에서 100건 단위로 읽는다.
2. Processor에서 검증 규칙을 적용한다.
3. 정상/경고/오류를 분류한다.
4. 오류와 경고를 `food_quality_issue`에 저장한다.
5. Tasklet에서 issue 유형별 건수를 `food_quality_report`에 저장한다.

### 테이블 설계안

```sql
CREATE TABLE food_quality_issue (
    issue_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_code VARCHAR(64),
    source_name VARCHAR(100),
    source_row_no INT,
    issue_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    issue_message VARCHAR(500),
    raw_value VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_food_quality_issue_type (issue_type),
    INDEX idx_food_quality_issue_food (food_code)
);

CREATE TABLE food_quality_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT,
    checked_count INT,
    error_count INT,
    warning_count INT,
    duplicate_conflict_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 연습 포인트

- 검증 전용 Processor 설계
- skip하지 않고 issue 테이블에 남기는 흐름
- 배치 실행 결과를 DB에 리포트로 저장
- 관리자 화면/API로 품질 리포트 조회 가능성 열기

### 현재 프로젝트에 붙였을 때 효과

식단 분석이 잘못된 영양값에 흔들리는 것을 줄일 수 있다. 특히 추천 음식이나 일일 목표 달성률 계산의 신뢰도를 높일 수 있다.

## 아이디어 5. 사용자 식단 기반 추천 후보 사전 계산 배치

### 목표

현재 음식 추천은 식단 입력 시점에 선택 음식과 목표 칼로리 차이를 기준으로 계산한다. 이 배치는 공공 음식 데이터를 활용해 사용자별/목표별 추천 후보를 미리 계산해두는 연습이다.

### 입력 데이터

- `users`
- `food_nutrition`
- `meals`
- `meal_foods`

### 배치 흐름

1. 활성 사용자 목록을 10명 단위로 읽는다.
2. 사용자별 일일 목표 칼로리와 목표 유형을 계산한다.
3. 목표가 `diet`, `health`, `muscle`인지에 따라 추천 기준을 다르게 둔다.
4. 단백질이 높은 음식, 칼로리가 낮은 음식, 지방이 낮은 음식 같은 후보를 점수화한다.
5. 사용자별 추천 후보 Top N을 `user_food_recommendation_snapshot`에 저장한다.
6. 매일 새벽 또는 음식 데이터 적재 후 자동 실행한다.

### 테이블 설계안

```sql
CREATE TABLE user_food_recommendation_snapshot (
    snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    food_code VARCHAR(64) NOT NULL,
    goal VARCHAR(50),
    meal_type VARCHAR(20),
    recommendation_score INT NOT NULL,
    reason VARCHAR(500),
    snapshot_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_food_snapshot (user_id, food_code, meal_type, snapshot_date),
    INDEX idx_user_food_snapshot_user_date (user_id, snapshot_date)
);
```

### 추천 점수 예시

| 사용자 목표 | 점수 기준 |
| --- | --- |
| `diet` | 낮은 칼로리, 낮은 지방, 높은 식이섬유 |
| `muscle` | 높은 단백질, 적정 칼로리 |
| `health` | 탄단지 균형, 낮은 나트륨 |

### 연습 포인트

- DB reader로 사용자 목록 읽기
- 사용자 1명에서 추천 row 여러 개 생성하기
- Processor에서 기존 `MealService.calculateDailyGoal`과 비슷한 계산 재사용
- 매일 새벽 스케줄 실행
- 기존 실시간 추천을 사전 계산 테이블 조회 방식으로 바꾸는 확장 가능성 확인

### 현재 프로젝트에 붙였을 때 효과

식단 등록 화면에서 추천 음식 조회가 빨라지고, 사용자의 목표에 맞는 개인화 추천을 보여줄 수 있다.

## 아이디어 6. CSV를 API처럼 나눠 가져오는 수집 시뮬레이션 배치

### 목표

데이터가 이미 로컬 CSV에 있더라도, 실제 공공데이터 API처럼 page/perPage 단위로 나눠 가져오는 상황을 연습한다.

이는 `batch_lab`에서 다룬 `AbstractPagingItemReader` 방식과 현재 프로젝트 데이터를 연결하는 연습에 적합하다.

### 구현 방식

1. CSV를 한 번에 읽는 대신 `CsvPageProvider` 같은 컴포넌트를 만든다.
2. `page`, `perPage`를 넘기면 해당 범위의 row만 반환하게 한다.
3. Reader는 `page=1`, `page=2`처럼 증가시키며 데이터를 가져온다.
4. 각 page 결과를 staging 테이블에 저장한다.
5. 실패 테스트를 위해 특정 page에서 예외를 발생시키고 restart를 연습한다.

### 테이블 설계안

```sql
CREATE TABLE batch_page_checkpoint (
    checkpoint_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    source_file VARCHAR(255) NOT NULL,
    last_success_page INT NOT NULL,
    per_page INT NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_batch_page_checkpoint (job_name, source_file)
);
```

### 연습 포인트

- API paging reader와 유사한 reader 구현
- JobParameter로 `pageSize`, `failAtPage` 받기
- 실패 후 재시작 시 이미 성공한 page를 건너뛰기
- 처리 완료 후 checkpoint 정리

### 현재 프로젝트에 붙였을 때 효과

나중에 실제 공공데이터 API를 붙일 때 구조를 거의 그대로 가져갈 수 있다. 네트워크 호출 대신 로컬 CSV로 먼저 안정적인 배치 흐름을 연습할 수 있다는 점이 좋다.

## 아이디어 7. 충청남도교육청 OpenAPI RestClient 수집 배치

### 목표

`data/충청남도교육청_충남방과후학교지원센터_음식별칼로리/openapi.json`을 기준으로, 실제 공공데이터 API를 `RestClient`로 page 단위 호출해 수집하는 배치를 구성한다.

이 아이디어는 `batch_lab`에서 다룬 `AbstractPagingItemReader`와 가장 직접적으로 연결된다. 이미 CSV가 있어도 학습 목적상 API 응답을 `page`, `perPage`로 나누어 가져오고, 중간 실패/재시작/중복 upsert를 연습할 수 있다.

### OpenAPI 기준

`openapi.json`에서 확인한 API 구조는 다음과 같다.

| 항목 | 값 |
| --- | --- |
| host | `api.odcloud.kr` |
| basePath | `/api` |
| path | `/15152347/v1/uddi:4fe3deb1-da97-4fe3-89a0-c2773b6a7bb6` |
| method | `GET` |
| query | `page`, `perPage`, `returnType`, `serviceKey` |
| auth | header `Authorization` 또는 query `serviceKey` |
| 응답 wrapper | `page`, `perPage`, `totalCount`, `currentCount`, `matchCount`, `data` |
| data item | `음식구분`, `음식명`, `칼로리` |

요청 URL 예시는 다음과 같다.

```text
https://api.odcloud.kr/api/15152347/v1/uddi:4fe3deb1-da97-4fe3-89a0-c2773b6a7bb6?page=1&perPage=10&returnType=JSON&serviceKey={SERVICE_KEY}
```

### 배치 Job 구성

```text
Job: cnAfterSchoolFoodCalorieApiImportJob

Step 1. apiCollectStep
- RestClient 기반 paging reader
- page=1부터 시작해 perPage 단위로 API 호출
- 응답 data가 비거나 누적 건수가 totalCount 이상이면 종료
- 수집 row를 api staging 테이블에 저장

Step 2. apiNormalizeStep
- 음식구분, 음식명, 칼로리 정제
- 칼로리 숫자 변환
- 음식명 누락/칼로리 오류 reject 처리
- 정제 성공 row를 food_nutrition 또는 별도 calorie catalog에 upsert

Step 3. apiImportReportStep
- page 수, 총 수집 건수, 성공/실패/중복 건수 저장
```

### 테이블 설계안

API 응답은 CSV 원본보다 단순하므로, API 원문 staging과 정제 테이블을 분리한다.

```sql
CREATE TABLE public_food_calorie_api_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT,
    page_no INT NOT NULL,
    per_page INT NOT NULL,
    row_no_in_page INT NOT NULL,
    food_type VARCHAR(100),
    food_name VARCHAR(200),
    raw_calorie VARCHAR(50),
    import_status VARCHAR(20) DEFAULT 'READY',
    error_message VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_api_food_page_row (page_no, per_page, row_no_in_page)
);

CREATE TABLE public_food_calorie_catalog (
    calorie_food_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_type VARCHAR(100),
    food_name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    calorie_kcal INT NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_public_food_calorie (normalized_name, food_type, source_name)
);

CREATE TABLE public_food_calorie_api_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_execution_id BIGINT,
    requested_per_page INT,
    total_count INT,
    collected_count INT,
    success_count INT,
    reject_count INT,
    duplicate_count INT,
    started_at DATETIME,
    finished_at DATETIME
);
```

`food_nutrition`에 바로 넣고 싶다면 `public_food_calorie_catalog` 대신 기존 테이블을 사용할 수 있다. 다만 OpenAPI 응답에는 탄수화물/단백질/지방이 없으므로, 기존 `food_nutrition`의 필수 영양 컬럼을 nullable로 바꾸거나 기본값을 넣어야 한다. 학습용으로는 별도 `public_food_calorie_catalog`에 먼저 저장하는 편이 깔끔하다.

### DTO 설계안

한글 JSON 필드는 `@JsonProperty`로 매핑한다.

```java
public record CnFoodCalorieApiResponse(
    int page,
    int perPage,
    int totalCount,
    int currentCount,
    int matchCount,
    List<CnFoodCalorieItem> data
) {
}

public record CnFoodCalorieItem(
    @JsonProperty("음식구분") String foodType,
    @JsonProperty("음식명") String foodName,
    @JsonProperty("칼로리") Integer calorie
) {
}
```

### Reader 설계안

`AbstractPagingItemReader`를 상속하면 page 기반 API를 자연스럽게 읽을 수 있다.

```java
@Bean
@StepScope
ItemReader<CnFoodCalorieItem> cnFoodCalorieApiReader(
        @Value("#{jobParameters['serviceKey']}") String serviceKey,
        @Value("#{jobParameters['perPage']}") Integer perPage) {

    RestClient restClient = RestClient.builder()
            .baseUrl("https://api.odcloud.kr/api")
            .build();

    return new AbstractPagingItemReader<>() {
        private int totalCount = Integer.MAX_VALUE;

        {
            setPageSize(perPage == null ? 10 : perPage);
        }

        @Override
        protected void doReadPage() {
            if (results == null) {
                results = new CopyOnWriteArrayList<>();
            } else {
                results.clear();
            }

            int page = getPage() + 1;
            if ((page - 1) * getPageSize() >= totalCount) {
                return;
            }

            CnFoodCalorieApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/15152347/v1/uddi:4fe3deb1-da97-4fe3-89a0-c2773b6a7bb6")
                            .queryParam("page", page)
                            .queryParam("perPage", getPageSize())
                            .queryParam("returnType", "JSON")
                            .queryParam("serviceKey", serviceKey)
                            .build())
                    .retrieve()
                    .body(CnFoodCalorieApiResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                totalCount = 0;
                return;
            }

            totalCount = response.totalCount();
            results.addAll(response.data());
        }
    };
}
```

### Processor 설계안

Processor에서는 API 응답을 staging/final 저장용 DTO로 바꾼다.

처리 규칙은 다음 정도면 충분하다.

- `foodName`이 비어 있으면 reject
- `calorie`가 `null` 또는 0 이하이면 reject
- `foodType`은 trim 후 비어 있으면 `기타`
- `normalizedName`은 공백 제거, 괄호 제거, 소문자화 기준으로 생성
- 같은 `normalizedName + foodType + sourceName`은 upsert

### 실행 파라미터 예시

```text
jobName=cnAfterSchoolFoodCalorieApiImportJob
serviceKey={공공데이터포털_SERVICE_KEY}
perPage=10
runId=20260529150000
```

학습 중에는 `perPage=5`나 `perPage=10`처럼 작게 두는 것이 좋다. 그래야 여러 번 API를 호출하고, chunk commit과 page 이동을 로그로 확인하기 쉽다.

### 실패/재시작 연습 포인트

이 배치는 일부러 장애를 넣기 좋다.

- `failAtPage=3` JobParameter를 추가해 3페이지에서 예외 발생
- 재시작 시 이미 staging에 들어간 page row는 unique key로 중복 방지
- `runId`를 빼고 같은 JobParameter로 재실행해 Spring Batch의 JobInstance 중복 제약 확인
- API 401/500 응답을 만났을 때 retry 또는 실패 처리
- serviceKey 누락 시 명확한 실패 메시지 기록

### 로컬 CSV와 병행하는 방법

실제 API 호출은 service key와 네트워크가 필요하므로, 같은 구조로 두 가지 reader를 교체 가능하게 만들면 좋다.

| profile 또는 parameter | Reader |
| --- | --- |
| `sourceMode=api` | RestClient paging reader |
| `sourceMode=csv` | 로컬 CSV paging simulation reader |

이렇게 만들면 평소에는 CSV로 반복 연습하고, 필요할 때만 실제 API를 호출할 수 있다.

### 현재 프로젝트에 붙였을 때 효과

이 API는 `음식구분`, `음식명`, `칼로리`만 제공하므로 정밀한 탄단지 분석용 데이터라기보다는 음식 검색 보강, 칼로리 참고값, 카테고리 기반 추천 후보로 쓰기 좋다. 기존 `food_nutrition`의 영양 상세 데이터와 결합하면 음식 검색 결과에서 “기본 영양 상세 데이터”와 “공공 API 칼로리 참고 데이터”를 함께 활용할 수 있다.

## 추천 우선순위

처음 구현한다면 다음 순서가 좋다.

| 순서 | 아이디어 | 이유 |
| --- | --- | --- |
| 1 | 음식 영양 카탈로그 정제/적재 | 현재 앱의 `food_nutrition`과 바로 연결되고 구현 범위가 명확함 |
| 2 | 영양 품질 검증 리포트 | skip/reject/report 같은 배치 핵심 패턴을 익히기 좋음 |
| 3 | 충청남도교육청 OpenAPI RestClient 수집 | 실제 page/perPage API를 호출하므로 `batch_lab`의 API reader 연습과 가장 잘 맞음 |
| 4 | CSV paging 수집 시뮬레이션 | API key나 네트워크 없이 같은 구조를 반복 연습할 수 있음 |
| 5 | 지역 음식 콘텐츠 적재 | 긴 텍스트와 1:N child row 생성 연습에 좋음 |
| 6 | 음식명 표준화/중복 병합 | 난이도는 높지만 데이터 품질 개선 효과가 큼 |
| 7 | 사용자별 추천 후보 사전 계산 | 앱 기능 고도화에 좋고 스케줄 배치로 확장 가능 |

## 가장 작은 MVP 제안

가장 먼저 만들 배치 MVP는 다음 정도가 적당하다.

```text
Job: foodNutritionImportJob

Step 1. foodNutritionCsvStageStep
- CSV를 25건 단위로 읽어 food_nutrition_staging에 원문 저장

Step 2. foodNutritionNormalizeStep
- staging에서 READY 상태 row를 읽어 숫자 변환/음식명 정제
- 성공 row는 food_nutrition에 upsert
- 실패 row는 staging.error_message에 기록

Step 3. foodNutritionImportReportStep
- 총 처리 건수, 성공 건수, 실패 건수를 food_import_report에 저장
```

이 MVP만 구현해도 Spring Batch의 reader, processor, writer, chunk, JobParameter, 재실행, upsert, 리포트 Tasklet을 모두 연습할 수 있다.
