# Spring Batch 교육 범위 정리

이 문서는 `./tmp/batch_lab` 프로젝트에서 실제로 다룬 내용만 기준으로, 현재까지 배운 기술 범위와 그 범위 안에서 작성할 수 있는 프로그램 수준을 정리한다.

## 1. 프로젝트가 다루는 기술 스택

`batch_lab`는 Spring Boot 기반 웹 애플리케이션 안에서 Spring Batch 작업을 수동 실행, 스케줄 실행, API 기반 실행으로 연결해 보는 교육용 프로젝트다.

주요 기술은 다음과 같다.

| 구분 | 다룬 내용 |
| --- | --- |
| Java | Java 21 |
| Spring Boot | 3.5.x 기반 애플리케이션 구성 |
| Spring Batch | Job, Step, Chunk, Tasklet, JobParameter, JobRepository, JobOperator, JobExplorer |
| 데이터 접근 | MyBatis, MyBatis Batch Reader/Writer |
| DB | MySQL, HikariCP, 다중 DataSource |
| 웹 | Spring MVC `@RestController`로 배치 실행 API 제공 |
| 스케줄링 | `@EnableScheduling`, `@Scheduled` |
| 테스트 | Spring Boot Test, MyBatis Test, DB 연결/Mapper 테스트 |

## 2. Spring Batch로 할 수 있게 된 것

### 2.1 배치 메타데이터 DB 구성

Spring Batch가 실행 이력, JobInstance, JobExecution, StepExecution 등을 저장할 별도 DB를 구성할 수 있다.

프로젝트에서는 `batch_system` DB를 Spring Batch 메타데이터 저장소로 사용한다.

- `spring.datasource.batch-system.*` 설정
- `spring.batch.jdbc.initialize-schema=always`
- `spring.batch.job.enabled=false`
- `@Primary` DataSource와 TransactionManager 구성

따라서 애플리케이션 시작 시 배치 Job이 자동 실행되지 않게 막고, REST API나 Scheduler에서 필요한 시점에 직접 Job을 실행하는 구조를 만들 수 있다.

### 2.2 다중 DB 연결 구성

이 프로젝트는 DB를 목적별로 분리한다.

| DB | 역할 |
| --- | --- |
| `batch_system` | Spring Batch 메타데이터 저장 |
| `sakila` | 원본 결제 데이터 조회 |
| `sakila_batch` | 집계 결과와 백업 데이터 저장 |

이를 위해 다음을 구성할 수 있다.

- `@ConfigurationProperties`로 DB별 DataSource 생성
- MyBatis `SqlSessionFactory`를 DB별로 분리
- `@MapperScan`의 `sqlSessionFactoryRef`로 Mapper 패키지와 DB 연결
- 쓰기 DB에는 별도 `PlatformTransactionManager` 지정
- `@Qualifier`로 Reader, Writer, Step에서 사용할 DB 명확히 선택

이 수준이면 원본 DB에서 읽고 별도 운영/분석/백업 DB에 저장하는 단순 ETL성 배치를 만들 수 있다.

### 2.3 Chunk 기반 배치 작성

`Reader -> Processor -> Writer` 흐름의 Chunk 지향 Step을 작성할 수 있다.

프로젝트에서 다룬 Reader/Writer는 다음과 같다.

| 구성 요소 | 사용 예 |
| --- | --- |
| `MyBatisCursorItemReader` | 일별 결제 집계 조회 |
| `MyBatisPagingItemReader` | 월별 결제 내역을 페이지 단위로 조회 |
| `AbstractPagingItemReader` | REST API를 페이지 단위로 호출해 읽기 |
| `ItemProcessor` | 날짜 필드 보정, 백업 기준월 설정, 빈 데이터 skip |
| `MyBatisBatchItemWriter` | 집계/백업 데이터를 MySQL에 upsert |

Chunk Step은 다음 수준까지 작성할 수 있다.

- `new StepBuilder(stepName, jobRepository)`로 Step 생성
- `.<Input, Output>chunk(size, transactionManager)` 지정
- Reader, Processor, Writer 연결
- Processor에서 `null` 반환으로 Writer 전달 제외
- Chunk 단위 commit과 write count 확인
- `ChunkListener`로 chunk 성공/실패 로그 기록

### 2.4 Job 구성과 Step 연결

단일 Step Job과 다중 Step Job을 모두 구성할 수 있다.

프로젝트에 있는 Job 유형은 다음과 같다.

| Job | 구성 |
| --- | --- |
| `dailyPaymentsAggregateJob` | 일별 결제 집계 Step 1개 |
| `paymentBackupJob` | 월별 결제 백업 Step 이후 리포트 Tasklet Step 실행 |
| `apiPaymentBackupJob` | API 기반 월별 백업 Step 이후 리포트 Tasklet Step 실행 |

이 범위 안에서는 다음을 작성할 수 있다.

- 하나의 Step만 실행하는 단순 Job
- 여러 Step을 순차 실행하는 Job
- Chunk Step 뒤에 Tasklet Step을 붙여 후처리/리포트 로그를 남기는 Job
- JobParameter로 날짜나 기준월을 받아 실행 시점마다 다른 데이터를 처리하는 Job

### 2.5 JobParameter와 Scope 사용

Job 실행 시 외부에서 전달한 파라미터를 Reader/Processor/Tasklet에서 사용할 수 있다.

프로젝트에서 사용한 파라미터는 다음과 같다.

| 파라미터 | 사용처 |
| --- | --- |
| `targetDate` | 일별 결제 집계 기준일 |
| `yearMonth` | 월별 결제 백업 기준월 |
| `runTime` | 같은 파라미터로 인한 JobInstance 중복 방지 |

이를 위해 다음을 사용할 수 있다.

- `@StepScope`
- `@JobScope`
- `@Value("#{jobParameters['...']}")`
- `jobExecution.getJobParameters()`

이 수준이면 `2026-05-29` 같은 특정 날짜, `2026-05` 같은 특정 월을 파라미터로 받아 선택적으로 처리하는 배치를 만들 수 있다.

### 2.6 REST API로 배치 실행

Spring MVC 컨트롤러에서 `JobOperator`를 사용해 배치를 실행할 수 있다.

프로젝트에서 제공하는 실행 API는 다음과 같은 형태다.

| API | 실행 Job |
| --- | --- |
| `GET /batch/daily-payments-report?targetDate=...` | `dailyPaymentsAggregateJob` |
| `GET /batch/payment-backup?yearMonth=...` | `paymentBackupJob` |
| `GET /batch/payment-backup-api?yearMonth=...` | `apiPaymentBackupJob` |

컨트롤러에서는 다음을 다룬다.

- `Properties`로 JobParameter 구성
- `jobOperator.start(jobName, params)`로 Job 시작
- `JobExplorer`로 실행 상태 조회
- 실행 ID, Job ID, 실행 상태 응답
- 실패한 JobExecution이 있을 때 `jobOperator.restart(...)`로 재시작 시도

이 범위 안에서는 관리자용 HTTP API를 통해 특정 날짜나 월의 배치를 수동 실행하는 기능을 만들 수 있다.

### 2.7 스케줄러로 배치 실행

Spring Scheduling을 사용해 정해진 시각에 배치를 실행할 수 있다.

프로젝트에서는 다음을 다룬다.

- `@EnableScheduling`
- `@Scheduled(fixedRate = ...)`
- `@Scheduled(cron = "0 0 1 * * *")`
- 매일 01시에 전일 기준 `dailyPaymentsAggregateJob` 실행

따라서 매일 새벽 전날 데이터를 집계하거나, 일정 간격으로 작업을 반복 실행하는 기본 스케줄 배치를 만들 수 있다.

### 2.8 DB 집계와 백업 처리

비즈니스 예제는 Sakila 결제 데이터를 대상으로 한다.

작성 가능한 처리 유형은 다음과 같다.

- 특정 날짜의 결제 건수, 총액, 평균 결제액 집계
- 집계 결과를 `payment_report` 테이블에 저장
- 같은 날짜 집계가 이미 있으면 `ON DUPLICATE KEY UPDATE`로 갱신
- 특정 월의 결제 상세 내역을 페이지 단위로 읽기
- 결제 상세를 `payment_backup` 테이블에 백업
- 같은 payment id가 이미 있으면 upsert
- 백업 시 `payment_ym`, `backup_date` 같은 배치 관리용 필드 추가

즉, 이 프로젝트 범위에서는 “원본 테이블 조회 -> 가공 -> 대상 테이블 저장” 형태의 기본 데이터 파이프라인을 구현할 수 있다.

### 2.9 API 기반 배치 입력

DB에서 직접 읽는 방식뿐 아니라, REST API를 호출해 데이터를 읽는 Reader도 작성한다.

프로젝트에서는 다음 흐름을 다룬다.

1. `/sakila/payment/{yearMonth}?page=...` API가 결제 목록을 페이지 단위로 제공
2. `AbstractPagingItemReader`가 page 값을 증가시키며 API 호출
3. `RestClient`로 응답 목록을 받아 Reader 결과에 추가
4. Processor에서 백업 필드를 보정
5. Writer가 백업 DB에 저장

이 수준이면 외부 API 또는 내부 API에서 페이지 단위로 데이터를 가져와 DB에 저장하는 단순 API 수집 배치를 만들 수 있다.

### 2.10 Tasklet 후처리

Chunk 처리 이후 별도 Tasklet Step을 실행할 수 있다.

프로젝트의 `GeneralReportTasklet`은 현재 JobExecution의 StepExecution 목록을 확인해, 자기 자신을 제외한 Step들의 write count 합계를 로그로 남긴다.

이 범위에서는 다음 정도의 Tasklet을 작성할 수 있다.

- JobParameter 조회
- Job 이름 조회
- 이전 Step 실행 결과 조회
- 처리 건수 요약 로그 출력
- 단발성 후처리 Step 구성

## 3. 테스트로 확인한 범위

프로젝트에는 다음 수준의 테스트가 포함되어 있다.

| 테스트 | 확인 내용 |
| --- | --- |
| `DBConfigTest` | 세 개의 DataSource, TransactionManager, SqlSessionFactory 등록과 DB 연결 |
| `SakilaMapperTest` | 원본 DB 일별 결제 집계 조회 |
| `SakilaBatchMapperTest` | 백업 DB 집계 결과 insert/update |
| `BatchApplicationTests` | Spring Context 로딩 |

테스트는 실제 MySQL DB에 의존한다. 따라서 테스트를 안정적으로 실행하려면 로컬에 `batch_system`, `sakila`, `sakila_batch` DB와 계정 정보가 준비되어 있어야 한다.

## 4. 이 프로젝트만 보고 작성할 수 있는 프로그램 수준

현재 교육 범위 안에서 자신 있게 작성할 수 있는 프로그램은 다음과 같다.

- MySQL 원본 DB에서 데이터를 읽어 다른 MySQL DB에 저장하는 배치
- 일/월 단위 기준 파라미터를 받아 실행되는 집계 배치
- `INSERT ... ON DUPLICATE KEY UPDATE` 기반의 재실행 가능한 저장 로직
- 대량 데이터를 페이지 또는 커서 방식으로 읽는 MyBatis 기반 Chunk 배치
- 배치 실행용 REST API
- 매일 특정 시각에 실행되는 스케줄 배치
- 간단한 실패 재시작 API
- API를 페이지 단위로 호출해 데이터를 적재하는 수집 배치
- 배치 완료 후 처리 건수 정도를 로그로 남기는 후처리 Tasklet
- DB 연결과 Mapper 동작을 확인하는 기본 통합 테스트

예를 들어 이 프로젝트 범위 안에서는 다음 같은 기능을 만들 수 있다.

- 매일 새벽 전날 주문/결제/접속 로그를 집계해 리포트 테이블에 저장
- 매월 특정 테이블의 데이터를 백업 테이블로 복사
- 내부 API에서 페이지 단위로 데이터를 가져와 적재
- 관리자 화면이나 API에서 특정 날짜의 배치를 재실행
- 실패한 배치를 마지막 실패 지점 이후로 재시작 시도

## 5. 아직 이 프로젝트만으로는 부족한 범위

다음 내용은 `batch_lab`에서 본격적으로 다루지 않았거나, 예제 수준을 넘어 추가 학습이 필요하다.

| 영역 | 현재 한계 |
| --- | --- |
| 복잡한 장애 처리 | skip/retry/backoff 정책을 체계적으로 구성하지 않음 |
| 병렬 처리 | partitioning, multi-threaded step, remote chunking 미사용 |
| 대용량 운영 최적화 | fetch size, 인덱스 전략, 락 경합, 장시간 트랜잭션 관리가 깊게 다뤄지지 않음 |
| 운영 모니터링 | 대시보드, 알림, metrics, tracing 미구성 |
| 보안 | 배치 실행 API 인증/인가 없음 |
| 배포/운영 | profile 분리, secret 관리, CI/CD, 운영 스케줄 관리 미포함 |
| 정교한 테스트 | JobLauncherTestUtils 등으로 Job/Step 단위 테스트를 충분히 검증하지 않음 |
| 파일 처리 | CSV, Excel, fixed-length file Reader/Writer 미사용 |
| 메시징 | Kafka, RabbitMQ 같은 메시지 기반 배치/스트림 연계 미사용 |

따라서 현재 수준은 “Spring Batch의 핵심 구조를 이해하고, DB/API 기반의 단순 ETL 배치를 구현할 수 있는 입문에서 초급 실전 사이”로 보는 것이 적절하다.

## 6. 현재 프로젝트에 적용할 때의 판단

현재 프로젝트에서 Spring Batch를 사용한다면, `batch_lab` 범위만으로는 다음과 같은 작업이 현실적이다.

- 외부/공공 데이터 또는 내부 DB 데이터를 주기적으로 가져와 로컬 DB에 저장
- 음식/영양/활동 기록을 하루 단위로 집계
- 사용자별 일일/월간 요약 테이블 생성
- 오래된 원본 데이터를 백업 테이블로 복사
- 관리자 API로 특정 기준일 배치를 수동 실행
- 매일 새벽 자동 집계 실행

반대로, 장애 허용성이 높은 대규모 배치, 수백만 건 이상 처리 최적화, 병렬 처리, 운영 알림까지 필요한 작업은 이 교육 범위만으로 바로 구현하기보다 추가 설계와 학습이 필요하다.

## 7. 핵심 사용 흐름 요약

Spring Batch 사용 흐름은 다음 순서로 잡으면 된다.

1. 배치 메타데이터용 DB와 업무용 DB를 분리해 DataSource를 구성한다.
2. 원본 데이터를 읽을 Reader를 만든다.
3. 필요한 값을 보정하거나 필터링하는 Processor를 만든다.
4. 대상 DB에 저장할 Writer를 만든다.
5. Reader, Processor, Writer를 Chunk Step으로 묶는다.
6. Step을 Job으로 묶는다.
7. `JobOperator` 또는 Scheduler에서 JobParameter와 함께 실행한다.
8. `JobExplorer`와 로그로 실행 상태와 처리 건수를 확인한다.

이 흐름을 따르면 `batch_lab`에서 다룬 범위 안에서 날짜/월 기준 집계, 백업, API 수집 배치를 작성할 수 있다.
