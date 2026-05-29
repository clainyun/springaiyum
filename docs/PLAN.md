# 영양성분 DB 배치 적재 구현 계획

## Summary

`docs/EDUCATION.md`의 Spring Batch 핵심 범위(Job/Step/Chunk/Processor/Writer/Tasklet/JobParameter/수동 실행/upsert)로 구현 가능하다. 단, `data/영양성분 DB`에는 XLSX 대용량 파일이 있으므로 **Apache POI streaming reader만 최소 추가**한다. 최종 목표는 영양성분 파일을 `staging -> 정제 -> food_nutrition upsert -> 리포트` 흐름으로 멱등 적재하는 것이다.

v1에서는 `food_nutrition`에 맞는 영양 데이터만 적재한다. `전북특별자치도_음식정보_20191219.csv`는 설명/레시피성 데이터라 `food_nutrition` 대상에서 제외하고, 별도 콘텐츠 배치 후보로 남긴다.

## Key Changes

- 의존성 추가:
  - `spring-boot-starter-batch`
  - `spring-batch-test`
  - `org.apache.poi:poi-ooxml` for XLSX streaming
- 설정 추가:
  - `spring.batch.job.enabled=false`
  - `spring.batch.jdbc.initialize-schema=always`
  - batch metadata는 최소 수정 원칙에 따라 기존 `ssafy_yumyumcoach` DataSource에 생성한다.
- 신규 테이블:
  - `nutrition_import_staging`: 원본 행, 정제 상태, 오류 메시지 저장
  - `nutrition_import_report`: 실행별 총 건수/성공/실패/upsert 요약 저장
- 최종 적재:
  - 기존 `food_nutrition` 테이블에 `food_code` 기준 `INSERT ... ON DUPLICATE KEY UPDATE`
  - 원본 `식품코드`가 있으면 그대로 사용
  - 없으면 `SRC_` + `SHA-256(sourceName|foodName|category|weight)` 기반 안정 키 생성

## Implementation Plan

1. 배치 스키마 준비
   - `src/main/resources`에 batch용 SQL을 추가하거나 기존 schema SQL 뒤에 staging/report 테이블을 추가한다.
   - `food_nutrition` 구조는 변경하지 않는다.
   - `nutrition_import_staging`에는 raw 컬럼을 문자열로 저장한다: `raw_food_code`, `raw_food_name`, `raw_category`, `raw_weight`, `raw_energy_kcal`, `raw_protein_g`, `raw_fat_g`, `raw_carbohydrate_g`, `raw_sugar_g`, `raw_sodium_mg`, `raw_cholesterol_mg`, `raw_saturated_fat_g`, `raw_trans_fat_g`, `raw_caffeine_mg`, `import_status`, `error_message`.

2. Reader 구성
   - `NutritionSourceProfile`을 만든다.
   - 파일명 패턴별 컬럼 alias를 고정한다.
   - XLSX는 POI SAX/streaming 방식으로 한 행씩 읽어 `RawNutritionRow`로 변환한다.
   - CSV는 `BufferedReader` 기반 `ItemStreamReader`로 읽는다.
   - `sourcePath`, `sourceName`, `chunkSize`를 JobParameter로 받는다.

3. Step 1: staging 적재
   - Job name: `nutritionImportJob`
   - Step name: `nutritionStageStep`
   - Reader: XLSX/CSV profile 기반 row reader
   - Processor: row 번호와 source 정보를 붙이고 raw field DTO 생성
   - Writer: `nutrition_import_staging`에 batch insert
   - chunk size 기본값: `100`
   - 대용량 가공식품 파일은 `chunkSize=500`까지 허용하되 기본 계획은 100으로 둔다.

4. Step 2: 정제 및 upsert
   - Step name: `nutritionNormalizeUpsertStep`
   - Reader: staging에서 `job_execution_id`와 `import_status='READY'` row를 page 단위 조회
   - Processor:
     - 음식명 필수 검증
     - 숫자 컬럼 `BigDecimal` 변환
     - `food_code` 생성 또는 원본 코드 사용
     - `weight`는 원문 그대로 저장
     - CSV의 농림수산 칼로리 데이터는 `나트륨`, `콜레스테롤` 값을 mg 기준으로 매핑한다.
   - Writer:
     - 성공 row는 `food_nutrition`에 upsert
     - 성공 staging row는 `DONE`
     - 실패 row는 `FAILED`와 `error_message` 기록

5. Step 3: 리포트 Tasklet
   - Step name: `nutritionImportReportStep`
   - `nutrition_import_staging`에서 실행별 총 row, 성공, 실패 건수를 집계한다.
   - `nutrition_import_report`에 저장한다.
   - 로그에 `jobExecutionId`, `sourceName`, `total`, `success`, `failed` 출력한다.

6. 실행 API
   - `GET /batch/nutrition-import?sourcePath=...&sourceName=...&chunkSize=100&runTime=...`
   - `JobOperator.start("nutritionImportJob", params)` 사용
   - 응답은 execution id와 status만 반환한다.
   - 실패한 동일 JobInstance 재시작은 기존 `JobOperator.restart(...)` 패턴을 따른다.

## Test Plan

- 단위 테스트:
  - `NutritionSourceProfile`이 파일별 헤더를 target field로 매핑하는지 검증
  - 숫자 변환 실패, 빈 음식명, 빈 칼로리 처리 검증
  - food_code 생성이 같은 입력에 대해 항상 같은 값인지 검증
- 통합 테스트:
  - 작은 테스트 XLSX/CSV fixture로 staging row가 생성되는지 확인
  - 같은 파일을 두 번 실행해 `food_nutrition` row가 중복 증가하지 않고 upsert되는지 확인
  - 실패 row가 `nutrition_import_staging.import_status='FAILED'`로 남는지 확인
- 수동 검증:
  - `농림수산식품교육문화정보원_칼로리 정보_20190926.csv`로 먼저 실행
  - 이후 `20251229_음식DB 19495건.xlsx` 실행
  - 마지막에 대용량 `20260429_가공식품_277074건.xlsx` 실행

## Assumptions

- `전북특별자치도_음식정보_20191219.csv`는 영양성분 적재 대상에서 제외한다.
- 건강기능식품 파일은 v1에서 읽되, 필수 매핑값이 부족한 row는 `FAILED`로 남긴다.
- `food_nutrition` 테이블은 현재 스키마를 유지한다.
- batch metadata는 별도 DB를 만들지 않고 기존 애플리케이션 DB에 생성한다.
- XLSX 직접 처리를 위해 Apache POI streaming reader 추가는 허용된 최소 확장으로 본다.
- 실제 구현 시 이 계획 내용을 `docs/PLAN.md`에 저장한다.
