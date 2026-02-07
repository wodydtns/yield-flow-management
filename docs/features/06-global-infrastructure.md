# 글로벌 인프라(Global Infrastructure)

## 개요
프로젝트 전반에 걸쳐 사용되는 공통 컴포넌트 및 설정을 제공합니다.

## 주요 컴포넌트

### 1. API 응답 포맷 (ApiResponse)
**파일**: `src/main/java/com/yieldflow/management/global/response/ApiResponse.java`

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    Error error;
    T data;

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder().build();
    }

    public static <T> ApiResponse<T> ok(T message) {
        return ApiResponse.<T>builder()
                .data(message)
                .build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(HttpStatus httpStatus, 
                                                           String errorCode,
                                                           String errorMessage) {
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.<T>builder()
                        .error(Error.of(errorCode, errorMessage))
                        .build());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String errorCode, String errorMessage) {
        public static Error of(String errorCode, String errorMessage) {
            return new Error(errorCode, errorMessage);
        }
    }
}
```

**사용 예시**:
```java
// 성공 응답 (데이터 없음)
return ApiResponse.ok();

// 성공 응답 (데이터 포함)
return ApiResponse.ok(user);

// 실패 응답
return ApiResponse.fail(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력입니다.");
```

**응답 형식**:
```json
// 성공
{
  "data": { ... }
}

// 실패
{
  "error": {
    "errorCode": "INVALID_INPUT",
    "errorMessage": "잘못된 입력입니다."
  }
}
```

### 2. 예외 처리 (Exception Handling)

#### DomainException
**파일**: `src/main/java/com/yieldflow/management/global/exception/DomainException.java`

```java
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DomainException extends RuntimeException {
    HttpStatus status;
    String code;

    public DomainException(DomainExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.status = exceptionCode.getStatus();
        this.code = exceptionCode.name();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
```

#### DomainExceptionCode
**파일**: `src/main/java/com/yieldflow/management/global/exception/DomainExceptionCode.java`

```java
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum DomainExceptionCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 누락되었습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다."),
    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Json 데이터 처리 중 에러가 발생하였습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다.");

    final HttpStatus status;
    final String message;
}
```

**사용 예시**:
```java
if (user == null) {
    throw new DomainException(DomainExceptionCode.USER_NOT_FOUND);
}
```

### 3. BaseEntity (공통 엔티티)
**파일**: `src/main/java/com/yieldflow/management/global/entity/BaseEntity.java`

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**기능**:
- `createdAt`: 생성 시간 자동 설정
- `updatedAt`: 수정 시간 자동 업데이트
- `@EntityListeners(AuditingEntityListener.class)`: JPA Auditing 활성화

**사용 예시**:
```java
@Entity
public class MarketCode extends BaseEntity {
    // createdAt, updatedAt 자동 관리
}
```

**JPA Auditing 활성화**:
```java
@SpringBootApplication
@EnableJpaAuditing
public class ManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagementApplication.class, args);
    }
}
```

### 4. Security Configuration
**파일**: `src/main/java/com/yieldflow/management/global/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    public static final String[] SECURITY_EXCLUDE_PATHS = {
        "/public/**", "/api/swagger-ui/**", "/swagger-ui/**", "/swagger-ui.html",
        "/api/v3/api-docs/**", "/v3/api-docs/**", "/favicon.ico", "/actuator/**",
        "/swagger-resources/**", "/external/**", "/api/auth/**"
    };

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SECURITY_EXCLUDE_PATHS).permitAll()
                .requestMatchers("/api/**").hasRole("USER")
                .anyRequest().authenticated())
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                        .error(ApiResponse.Error.of("UNAUTHORIZED", "Authentication required"))
                        .build();
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**주요 설정**:
- CSRF 비활성화
- 세션 정책: `IF_REQUIRED`
- 인증 제외 경로 설정
- 비밀번호 암호화: BCrypt

### 5. Redis Configuration
**파일**: `src/main/java/com/yieldflow/management/global/config/RedisConfig.java`

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("springSessionDefaultRedisSerializer") RedisSerializer<Object> jsonSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
```

**기능**:
- Redis 세션 저장
- Key: String 직렬화
- Value: JSON 직렬화

### 6. Enums

#### UserRole
```java
public enum UserRole {
    USER,
    ADMIN
}
```

#### UserStatus
```java
public enum UserStatus {
    ACTIVE,
    BANNED,
    WITHDRAWN
}
```

## 설정 파일

### application.yml
```yaml
spring:
  application:
    name: yieldflow-management

  # 세션 설정
  session:
    store-type: redis
    redis:
      namespace: spring:session
      flush-mode: on-save

  # 데이터베이스 설정
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:yieldflow_management}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:yieldflow}
    password: ${DB_PASSWORD:password}
    hikari:
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      maximum-pool-size: 20
      minimum-idle: 5

  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

  # SQL 초기화
  sql:
    init:
      mode: always
      schema-locations: classpath:spring.sql
      encoding: UTF-8
      continue-on-error: true

  # Redis 설정
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 3000ms

  # 배치 설정
  batch:
    jdbc:
      initialize-schema: never
    job:
      enabled: false
    chunk:
      size: 100

# 로깅 설정
logging:
  level:
    com.yieldflow.management: DEBUG
    org.springframework.cache: DEBUG
    org.hibernate.SQL: DEBUG
```

## 구현 패턴

### 1. Record 사용 (DTO)
```java
// ❌ 기존 방식 (Lombok @Data)
@Data
public class UserDto {
    private String email;
    private String nickname;
}

// ✅ 권장 방식 (Record)
public record UserDto(
    String email,
    String nickname
) {}
```

### 2. Constructor Injection
```java
// ✅ 권장 방식
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

### 3. Optional 사용
```java
// ❌ 피해야 할 방식
Optional<User> user = userRepository.findByEmail(email);
if (user.isPresent()) {
    return user.get();
}

// ✅ 권장 방식
return userRepository.findByEmail(email)
    .orElseThrow(() -> new DomainException(DomainExceptionCode.USER_NOT_FOUND));
```

### 4. Stream API
```java
// ❌ 기존 방식
List<String> markets = new ArrayList<>();
for (MarketCode code : codes) {
    markets.add(code.getMarket());
}

// ✅ 권장 방식
var markets = codes.stream()
    .map(MarketCode::getMarket)
    .toList();
```

### 5. Switch Expression (Java 21)
```java
// ✅ 권장 방식
return switch (status) {
    case ACTIVE -> "활성";
    case BANNED -> "정지";
    case WITHDRAWN -> "탈퇴";
};
```

## 구현 시 참고사항

1. **Record 사용**: DTO는 불변 객체로 구현
2. **Constructor Injection**: `@RequiredArgsConstructor` 사용
3. **BaseEntity 상속**: 생성/수정 시간 자동 관리
4. **DomainException**: 비즈니스 예외는 DomainException 사용
5. **ApiResponse**: 모든 API 응답은 ApiResponse로 통일
6. **Validation**: Jakarta Validation 사용
7. **Logging**: SLF4J + Logback 사용
8. **환경 변수**: 민감 정보는 환경 변수로 관리
9. **Virtual Threads**: Java 21 Virtual Threads 활용
10. **Pattern Matching**: instanceof 대신 pattern matching 사용
