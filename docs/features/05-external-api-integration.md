# 외부 API 연동(External API Integration) 기능

## 개요
OpenFeign을 사용하여 빗썸(Bithumb) API와 통신하는 기능을 제공합니다.

## 아키텍처

### Feign Client 구조
```
Service Layer
    ↓
BithumbFeignService
    ↓
BithumbFeignClient (Interface)
    ↓
FeignConfig (Interceptor)
    ↓
External API (Bithumb)
```

## 주요 컴포넌트

### 1. Feign Client Interface
**파일**: `src/main/java/com/yieldflow/management/global/external/bithumb/client/BithumbFeignClient.java`

```java
@FeignClient(
    name = "bithumbClient", 
    url = "${bithumb.api-url}", 
    configuration = FeignConfig.class
)
public interface BithumbFeignClient {
    
    @GetMapping("/v1/accounts")
    List<BithumbAccountResponseDto> getAccounts();

    @GetMapping("/v1/orders/chance")
    OrderChanceResponseDto getOrderChance(@RequestParam("market") String market);

    @GetMapping("/v1/market/all")
    List<BithumbMarketCodeResponseDto> getMarketCodes();

    @GetMapping("v1/market/virtual_asset_warning")
    List<BithumbVirtualAssetWarning> getVirtualAssetWarning();
}
```

**지원 API**:
- 계좌 조회
- 주문 가능 정보 조회
- 마켓 코드 조회
- 가상자산 유의종목 조회

### 2. Feign Service
**파일**: `src/main/java/com/yieldflow/management/global/external/bithumb/service/BithumbFeignService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbFeignService {
    private final BithumbFeignClient bithumbFeignClient;

    public List<BithumbAccountResponseDto> getAccounts() {
        log.info("Fetching Bithumb accounts using Feign");
        List<BithumbAccountResponseDto> accounts = bithumbFeignClient.getAccounts();
        log.info("Fetched {} accounts", accounts.size());
        return accounts;
    }

    public OrderChanceResponseDto getOrderChance(String market) {
        log.info("Fetching order chance for market: {}", market);
        OrderChanceResponseDto orderChance = bithumbFeignClient.getOrderChance(market);
        log.info("Order chance fetched successfully for market: {}", market);
        return orderChance;
    }

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        log.info("Fetching market codes using Feign");
        List<BithumbMarketCodeResponseDto> marketCodes = bithumbFeignClient.getMarketCodes();
        log.info("Fetched {} market codes", marketCodes.size());
        return marketCodes;
    }

    public List<BithumbVirtualAssetWarning> getVirtualAssetWarning() {
        log.info("Fetching virtual asset warning using Feign");
        List<BithumbVirtualAssetWarning> virtualAssetWarning = bithumbFeignClient.getVirtualAssetWarning();
        log.info("Fetched {} virtual asset warning", virtualAssetWarning.size());
        return virtualAssetWarning;
    }
}
```

**역할**:
- Feign Client 호출
- 로깅 추가
- 예외 처리

### 3. Feign Configuration
**파일**: `src/main/java/com/yieldflow/management/global/config/FeignConfig.java`

```java
@Configuration
public class FeignConfig {
    
    @Value("${bithumb.access-key}")
    private String accessKey;
    
    @Value("${bithumb.secret-key}")
    private String secretKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // JWT 토큰 생성
            String token = generateJWT();
            
            // Authorization 헤더 추가
            requestTemplate.header("Authorization", "Bearer " + token);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    private String generateJWT() {
        // JWT 생성 로직
        // access-key, secret-key 사용
        return JwtUtil.createToken(accessKey, secretKey);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3);
    }
}
```

**주요 기능**:
1. **RequestInterceptor**: 모든 요청에 JWT 토큰 자동 추가
2. **ErrorDecoder**: Feign 에러를 도메인 예외로 변환
3. **Retryer**: 실패 시 재시도 정책 (최대 3회)

### 4. DTOs (Records)

#### BithumbAccountResponseDto
```java
public record BithumbAccountResponseDto(
    String currency,
    String balance,
    String locked,
    String avgBuyPrice,
    boolean avgBuyPriceModified,
    String unitCurrency
) {}
```

#### BithumbMarketCodeResponseDto
```java
public record BithumbMarketCodeResponseDto(
    String market,
    String koreanName,
    String englishName,
    String marketWarning
) {}
```

#### BithumbVirtualAssetWarning
```java
public record BithumbVirtualAssetWarning(
    String market,
    String warningType,
    String warningMessage
) {}
```

#### OrderChanceResponseDto
```java
public record OrderChanceResponseDto(
    String bidFee,
    String askFee,
    MarketInfo market,
    BidAccount bidAccount,
    AskAccount askAccount
) {
    public record MarketInfo(
        String id,
        String name,
        List<String> orderTypes,
        List<String> orderSides
    ) {}
    
    public record BidAccount(
        String currency,
        String balance,
        String locked,
        String avgBuyPrice
    ) {}
    
    public record AskAccount(
        String currency,
        String balance,
        String locked,
        String avgBuyPrice
    ) {}
}
```

## 설정

### application.yml
```yaml
bithumb:
  api-url: https://api.bithumb.com
  access-key: ${BITHUMB_ACCESS_KEY}
  secret-key: ${BITHUMB_SECRET_KEY}
```

### 환경 변수
```bash
export BITHUMB_ACCESS_KEY=your-access-key
export BITHUMB_SECRET_KEY=your-secret-key
```

## JWT 인증

### JWT 생성 로직
```java
public class JwtUtil {
    public static String createToken(String accessKey, String secretKey) {
        Map<String, Object> claims = Map.of(
            "access_key", accessKey,
            "nonce", UUID.randomUUID().toString()
        );
        
        return Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
}
```

**JWT 구성**:
- `access_key`: API 액세스 키
- `nonce`: 고유 식별자 (재사용 공격 방지)
- 서명: HMAC-SHA256

## 에러 처리

### FeignErrorDecoder
```java
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new DomainException(DomainExceptionCode.INVALID_REQUEST);
            case 401 -> new DomainException(DomainExceptionCode.UNAUTHORIZED_ACCESS);
            case 404 -> new DomainException(DomainExceptionCode.RESOURCE_NOT_FOUND);
            case 429 -> new DomainException(DomainExceptionCode.RATE_LIMIT_EXCEEDED);
            case 500 -> new DomainException(DomainExceptionCode.EXTERNAL_API_ERROR);
            default -> new Exception("Generic error");
        };
    }
}
```

## 재시도 정책

### Retryer 설정
```java
@Bean
public Retryer retryer() {
    // period: 초기 대기 시간 (1초)
    // maxPeriod: 최대 대기 시간 (3초)
    // maxAttempts: 최대 재시도 횟수 (3회)
    return new Retryer.Default(1000, 3000, 3);
}
```

**재시도 시나리오**:
1. 첫 번째 실패 → 1초 대기 후 재시도
2. 두 번째 실패 → 2초 대기 후 재시도
3. 세 번째 실패 → 3초 대기 후 재시도
4. 네 번째 실패 → 예외 발생

## 사용 예시

### Service에서 사용
```java
@Service
@RequiredArgsConstructor
public class MarketService {
    private final BithumbFeignService bithumbFeignService;

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        return bithumbFeignService.getMarketCodes();
    }
}
```

### Controller에서 사용
```java
@RestController
@RequiredArgsConstructor
public class UserController {
    private final BithumbFeignService bithumbFeignService;

    @GetMapping("/bithumb-account")
    public ApiResponse<List<BithumbAccountResponseDto>> getUserBithumbAccount() {
        return ApiResponse.ok(bithumbFeignService.getAccounts());
    }
}
```

## 로깅

### 요청/응답 로깅
```yaml
logging:
  level:
    com.yieldflow.management.global.external.bithumb: DEBUG
```

**로그 예시**:
```
2024-02-07 14:30:00 [http-nio-8080-exec-1] INFO  BithumbFeignService - Fetching market codes using Feign
2024-02-07 14:30:01 [http-nio-8080-exec-1] INFO  BithumbFeignService - Fetched 200 market codes
```

## 구현 시 참고사항

1. **@FeignClient**: `name`, `url`, `configuration` 필수 지정
2. **RequestInterceptor**: 모든 요청에 공통 헤더 추가
3. **ErrorDecoder**: Feign 에러를 도메인 예외로 변환
4. **Retryer**: 네트워크 장애 대비 재시도 정책 설정
5. **JWT 인증**: 매 요청마다 새로운 토큰 생성 (nonce 포함)
6. **Record 사용**: DTO는 불변 객체로 구현
7. **로깅**: 요청/응답 시 로그 기록으로 디버깅 용이
8. **환경 변수**: 민감 정보는 환경 변수로 관리
9. **타임아웃**: 적절한 타임아웃 설정 (기본 60초)
10. **Virtual Threads**: Java 21 Virtual Threads로 동시 요청 효율적 처리
