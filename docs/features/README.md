# YieldFlow Management - 기능별 참고 문서

## 개요
이 문서들은 YieldFlow Management 프로젝트의 기능별 구현 패턴과 참고 자료를 제공합니다.
새로운 기능을 구현할 때 해당 문서를 참고하여 일관된 코드 스타일과 아키텍처를 유지하세요.

## 문서 목록

### 1. [인증(Authentication)](./01-authentication.md)
- 세션 기반 로그인/로그아웃
- Spring Security 설정
- 비밀번호 암호화
- 세션 관리 (Redis)

**주요 컴포넌트**:
- `AuthController`
- `AuthService`
- `SecurityConfig`
- `LoginRequestDto`, `LoginResponseDto`

**엔드포인트**:
- `POST /api/auth/login`
- `POST /api/auth/logout`

---

### 2. [사용자 관리(User Management)](./02-user-management.md)
- 사용자 생성
- 이메일 인증
- 빗썸 계정 조회

**주요 컴포넌트**:
- `UserController`
- `UserService`
- `User` Entity
- `UserRequestDto`

**엔드포인트**:
- `POST /api/users`
- `GET /api/users/bithumb-account`

**검증 규칙**:
- 비밀번호: 8~16자, 대문자/숫자/특수문자 포함
- 이메일 중복 검증

---

### 3. [마켓 데이터(Market Data)](./03-market-data.md)
- 마켓 코드 조회
- 가상자산 유의종목 조회
- Feign Client를 통한 외부 API 연동

**주요 컴포넌트**:
- `MarketController`
- `MarketService`
- `BithumbFeignService`
- `MarketCode` Entity

**엔드포인트**:
- `GET /api/market/market-codes`
- `GET /api/market/virtual-asset-warning`

**필터링**:
- BTC, ETH, USDT, USDC만 조회

---

### 4. [배치 작업(Batch Jobs)](./04-batch-jobs.md)
- Spring Batch를 사용한 데이터 동기화
- 스케줄링 (Cron)
- Reader-Processor-Writer 패턴

**주요 컴포넌트**:
- `MarketCodeSyncJobConfig`
- `BatchJobController`
- `ItemReader`, `ItemProcessor`, `ItemWriter`

**스케줄**:
- 매 30분마다 자동 실행
- 수동 실행 API 제공

**엔드포인트**:
- `POST /api/batch/market-codes/sync`

---

### 5. [외부 API 연동(External API Integration)](./05-external-api-integration.md)
- OpenFeign을 사용한 HTTP 클라이언트
- JWT 인증
- 에러 처리 및 재시도

**주요 컴포넌트**:
- `BithumbFeignClient`
- `BithumbFeignService`
- `FeignConfig`
- `RequestInterceptor`, `ErrorDecoder`, `Retryer`

**지원 API**:
- 계좌 조회
- 주문 가능 정보 조회
- 마켓 코드 조회
- 가상자산 유의종목 조회

---

### 6. [글로벌 인프라(Global Infrastructure)](./06-global-infrastructure.md)
- 공통 컴포넌트
- 설정 파일
- 예외 처리
- 응답 포맷

**주요 컴포넌트**:
- `ApiResponse<T>`
- `DomainException`, `DomainExceptionCode`
- `BaseEntity`
- `SecurityConfig`, `RedisConfig`

**구현 패턴**:
- Record 사용 (DTO)
- Constructor Injection
- Optional 활용
- Stream API
- Switch Expression (Java 21)

---

## 프로젝트 구조

```
src/main/java/com/yieldflow/management/
├── domain/                    # 도메인별 기능
│   ├── auth/                 # 인증
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── dto/
│   ├── user/                 # 사용자 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── market/               # 마켓 데이터
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── batch/           # 배치 작업
│   └── order/                # 주문
│       ├── controller/
│       └── dto/
└── global/                    # 글로벌 설정
    ├── config/               # 설정 클래스
    ├── entity/               # 공통 엔티티
    ├── enums/                # Enum 클래스
    ├── exception/            # 예외 처리
    ├── external/             # 외부 API 연동
    │   └── bithumb/
    │       ├── client/
    │       ├── service/
    │       └── dto/
    ├── response/             # 응답 포맷
    └── util/                 # 유틸리티
```

## 기술 스택

### Backend
- **Java 21** - Virtual Threads, Records, Pattern Matching
- **Spring Boot 3.5.9** - 최신 Spring Framework
- **Spring Security** - 세션 기반 인증
- **Spring Batch** - 배치 작업
- **Spring Data JPA** - ORM
- **OpenFeign** - HTTP 클라이언트

### Database
- **PostgreSQL** - 메인 데이터베이스
- **Redis** - 세션 저장소

### Infrastructure
- **Docker Compose** - 컨테이너 오케스트레이션
- **Gradle** - 빌드 도구

## 코딩 규칙

### Java 21 Modern Features
1. **Records**: DTO는 항상 Record로 구현
2. **Virtual Threads**: 동기 I/O 스타일 선호
3. **Pattern Matching**: instanceof 대신 사용
4. **Switch Expression**: 향상된 switch 사용
5. **Stream API**: `toList()` 직접 사용

### Spring Boot 3.x Best Practices
1. **Jakarta EE**: `jakarta.*` 패키지 사용
2. **Constructor Injection**: `@RequiredArgsConstructor` 사용
3. **RestClient**: 동기 HTTP 호출 시 사용
4. **Validation**: Jakarta Validation 활용

### Code Style
1. **Immutability**: 불변 컬렉션 선호
2. **Optional**: 반환 타입에만 사용
3. **Logging**: SLF4J 사용
4. **Exception**: DomainException으로 통일

## 새 기능 구현 가이드

### 1. 도메인 기능 추가
```
1. domain/{domain_name}/ 패키지 생성
2. controller/ - REST API 엔드포인트
3. service/ - 비즈니스 로직
4. repository/ - 데이터 액세스
5. entity/ - JPA 엔티티
6. dto/ - Record 기반 DTO
```

### 2. 외부 API 연동
```
1. global/external/{api_name}/ 패키지 생성
2. client/ - Feign Client 인터페이스
3. service/ - 서비스 래퍼
4. dto/ - Record 기반 응답 DTO
5. FeignConfig - 인증/에러 처리 설정
```

### 3. 배치 작업 추가
```
1. domain/{domain_name}/batch/ 패키지 생성
2. {JobName}JobConfig - Job 설정
3. ItemReader - 데이터 읽기
4. ItemProcessor - 데이터 변환
5. ItemWriter - 데이터 저장
6. @Scheduled - 스케줄링
```

### 4. 공통 컴포넌트 추가
```
1. global/config/ - 설정 클래스
2. global/exception/ - 예외 클래스
3. global/util/ - 유틸리티 클래스
4. global/enums/ - Enum 클래스
```

## 테스트 작성 가이드

### JUnit 5 + AssertJ
```java
@DisplayName("사용자 생성 테스트")
@Test
void createUser() {
    // Given
    var userDto = new UserRequestDto("test@example.com", "Password123!", "테스트");
    
    // When
    userService.createUser(userDto);
    
    // Then
    var user = userRepository.findByEmail("test@example.com");
    assertThat(user).isNotNull();
    assertThat(user.getNickname()).isEqualTo("테스트");
}
```

### Testcontainers (Integration Test)
```java
@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void test() {
        // 실제 DB 환경에서 테스트
    }
}
```

## 참고 자료

- [Spring Boot 3.x Documentation](https://spring.io/projects/spring-boot)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [Spring Batch Documentation](https://spring.io/projects/spring-batch)
- [OpenFeign Documentation](https://spring.io/projects/spring-cloud-openfeign)

## 문의 및 기여

새로운 기능 구현 시 이 문서들을 참고하여 일관된 코드 스타일을 유지해주세요.
문서 개선 사항이나 추가 요청 사항이 있다면 팀에 공유해주세요.
