# 배치 작업(Batch Jobs) 기능

## 개요
Spring Batch를 사용하여 마켓 코드를 주기적으로 동기화하는 배치 작업을 제공합니다.

## 아키텍처

### Spring Batch 구조
```
Scheduler (@Scheduled)
    ↓
JobLauncher
    ↓
Job (marketCodeSyncJob)
    ↓
Step (marketCodeSyncStep)
    ↓
Reader → Processor → Writer
```

## 주요 컴포넌트

### 1. Batch Configuration
**파일**: `src/main/java/com/yieldflow/management/global/config/BatchConfig.java`

```java
@Configuration
public class BatchConfig {
    // Spring Boot Auto-configuration 사용
    // JobRepository, JobLauncher 자동 구성
}
```

### 2. Job Configuration
**파일**: `src/main/java/com/yieldflow/management/domain/market/batch/MarketCodeSyncJobConfig.java`

```java
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@EnableScheduling
public class MarketCodeSyncJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;
    private final BithumbFeignService bithumbFeignService;
    private final MarketCodeRepository marketCodeRepository;

    private static final String JOB_NAME = "marketCodeSyncJob";
    private static final String STEP_NAME = "marketCodeSyncStep";
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job marketCodeSyncJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(marketCodeSyncStep())
                .build();
    }

    @Bean
    public Step marketCodeSyncStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<BithumbMarketCodeResponseDto, MarketCode>chunk(CHUNK_SIZE, transactionManager)
                .reader(marketCodeReader())
                .processor(marketCodeProcessor())
                .writer(marketCodeWriter())
                .build();
    }
}
```

### 3. ItemReader
**빗썸 API에서 마켓 코드 조회**

```java
@Bean
public ItemReader<BithumbMarketCodeResponseDto> marketCodeReader() {
    log.info("Reading market codes from Bithumb API");
    List<BithumbMarketCodeResponseDto> marketCodes = bithumbFeignService.getMarketCodes();
    log.info("Fetched {} market codes from Bithumb API", marketCodes.size());
    return new ListItemReader<>(marketCodes);
}
```

**특징**:
- `ListItemReader` 사용 (메모리 기반)
- Feign Client로 외부 API 호출
- 전체 데이터를 한 번에 로드

### 4. ItemProcessor
**데이터 변환 및 Upsert 로직**

```java
@Bean
public ItemProcessor<BithumbMarketCodeResponseDto, MarketCode> marketCodeProcessor() {
    return dto -> {
        var existing = marketCodeRepository.findByMarket(dto.market());

        if (existing.isPresent()) {
            // 기존 데이터 업데이트
            var marketCode = existing.get();
            marketCode.updateInfo(dto.koreanName(), dto.englishName(), dto.marketWarning());
            log.debug("Updating market code: {}", dto.market());
            return marketCode;
        } else {
            // 신규 데이터 생성
            log.debug("Creating new market code: {}", dto.market());
            return MarketCode.builder()
                    .market(dto.market())
                    .koreanName(dto.koreanName())
                    .englishName(dto.englishName())
                    .marketWarning(dto.marketWarning())
                    .build();
        }
    };
}
```

**로직**:
1. 마켓 코드로 기존 데이터 조회
2. 존재하면 업데이트 (UPDATE)
3. 없으면 신규 생성 (INSERT)

### 5. ItemWriter
**데이터베이스 저장**

```java
@Bean
public ItemWriter<MarketCode> marketCodeWriter() {
    return items -> {
        log.info("Writing {} market codes to database", items.size());
        marketCodeRepository.saveAll(items);
        log.info("Successfully saved market codes");
    };
}
```

**특징**:
- `saveAll()` 사용으로 배치 저장
- Chunk 단위 (100개) 처리

## 스케줄링

### Cron 스케줄러
```java
@Scheduled(cron = "0 */30 * * * *")
public void runMarketCodeSyncJob() {
    try {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobLauncher.run(marketCodeSyncJob(), jobParameters);
        log.info("Scheduled market code sync job completed. JobExecutionId: {}, Status: {}",
                execution.getId(), execution.getStatus());
    } catch (Exception e) {
        log.error("Failed to run scheduled market code sync job", e);
    }
}
```

**스케줄 설정**:
- `0 */30 * * * *`: 매 30분마다 실행
- `JobParameters`에 timestamp 추가로 매번 고유한 Job 인스턴스 생성

### Cron 표현식 예시
```
0 */5 * * * *   → 매 5분마다
0 0 * * * *     → 매 시간 정각
0 0 0 * * *     → 매일 자정
0 0 9 * * MON   → 매주 월요일 오전 9시
```

## 수동 실행 API

### BatchJobController
**파일**: `src/main/java/com/yieldflow/management/domain/market/controller/BatchJobController.java`

```java
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchJobController {
    private final JobLauncher jobLauncher;
    private final Job marketCodeSyncJob;

    @PostMapping("/market-codes/sync")
    public ResponseEntity<ApiResponse<String>> runMarketCodeSync() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("run.id", LocalDateTime.now().toString())
                    .toJobParameters();

            var execution = jobLauncher.run(marketCodeSyncJob, jobParameters);

            log.info("Market code sync job started. JobExecutionId: {}, Status: {}",
                    execution.getId(), execution.getStatus());

            return ResponseEntity.ok(ApiResponse.ok(
                    "Market code sync job started. JobExecutionId: " + execution.getId()));

        } catch (Exception e) {
            log.error("Failed to run market code sync job", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<String>builder()
                            .error(ApiResponse.Error.of("BATCH_ERROR", e.getMessage()))
                            .build());
        }
    }
}
```

**엔드포인트**:
- `POST /api/batch/market-codes/sync` - 배치 작업 수동 실행

## 설정

### application.yml
```yaml
spring:
  batch:
    jdbc:
      initialize-schema: never  # 스키마 자동 생성 안 함
    job:
      enabled: false  # 애플리케이션 시작 시 자동 실행 방지
    chunk:
      size: 100  # 청크 크기
```

### 배치 메타데이터 테이블
**파일**: `src/main/resources/spring.sql`

Spring Batch가 사용하는 메타데이터 테이블:
- `BATCH_JOB_INSTANCE` - Job 인스턴스
- `BATCH_JOB_EXECUTION` - Job 실행 이력
- `BATCH_JOB_EXECUTION_PARAMS` - Job 파라미터
- `BATCH_STEP_EXECUTION` - Step 실행 이력
- `BATCH_STEP_EXECUTION_CONTEXT` - Step 실행 컨텍스트
- `BATCH_JOB_EXECUTION_CONTEXT` - Job 실행 컨텍스트

## 실행 흐름

```
1. Scheduler 트리거 (30분마다)
   ↓
2. JobLauncher.run() 호출
   ↓
3. Job 시작 (marketCodeSyncJob)
   ↓
4. Step 실행 (marketCodeSyncStep)
   ↓
5. Reader: 빗썸 API에서 데이터 조회
   ↓
6. Processor: DTO → Entity 변환 (Upsert 로직)
   ↓
7. Writer: DB에 100개씩 배치 저장
   ↓
8. Job 완료 및 로그 기록
```

## 사용 예시

### 수동 실행
```bash
curl -X POST http://localhost:8080/api/batch/market-codes/sync \
  -H "Cookie: SESSION=..."
```

**응답 예시**:
```json
{
  "data": "Market code sync job started. JobExecutionId: 1"
}
```

### 실행 이력 조회 (DB)
```sql
SELECT 
    je.job_execution_id,
    ji.job_name,
    je.status,
    je.start_time,
    je.end_time,
    je.exit_code
FROM batch_job_execution je
JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
ORDER BY je.start_time DESC
LIMIT 10;
```

## 구현 시 참고사항

1. **JobParameters 필수**: 매 실행마다 고유한 파라미터 필요 (timestamp 사용)
2. **Chunk 크기**: 메모리와 성능을 고려하여 적절히 설정 (기본 100)
3. **트랜잭션**: Step 단위로 트랜잭션 관리
4. **재시작 가능**: 실패 시 중단된 지점부터 재시작 가능
5. **멱등성**: Processor에서 Upsert 로직으로 중복 실행 방지
6. **로깅**: 각 단계별 로그 기록으로 추적 가능
7. **에러 처리**: try-catch로 예외 처리 및 로그 기록
8. **스케줄링**: `@EnableScheduling` 필수
9. **Virtual Threads**: Java 21 Virtual Threads로 효율적인 동시성 처리
