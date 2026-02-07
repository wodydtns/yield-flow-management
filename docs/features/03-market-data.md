# 마켓 데이터(Market Data) 기능

## 개요
빗썸 API를 통해 마켓 코드 및 가상자산 유의종목 정보를 조회하는 기능을 제공합니다.

## 아키텍처

### 레이어 구조
```
Controller (MarketController)
    ↓
Service (MarketService)
    ↓
External Service (BithumbFeignService)
    ↓
Feign Client (BithumbFeignClient)
```

## 주요 컴포넌트

### 1. Controller
**파일**: `src/main/java/com/yieldflow/management/domain/market/controller/MarketController.java`

```java
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @GetMapping("/market-codes")
    public ApiResponse<List<BithumbMarketCodeResponseDto>> getMarketCodes();

    @GetMapping("/virtual-asset-warning")
    public ApiResponse<List<BithumbVirtualAssetWarning>> getVirtualAssetWarning();
}
```

**엔드포인트**:
- `GET /api/market/market-codes` - 마켓 코드 목록 조회
- `GET /api/market/virtual-asset-warning` - 가상자산 유의종목 조회

### 2. Service
**파일**: `src/main/java/com/yieldflow/management/domain/market/service/MarketService.java`

```java
@Service
@RequiredArgsConstructor
public class MarketService {
    private final BithumbFeignService bithumbFeignService;
    
    private static final Set<String> TARGET_CURRENCIES = Set.of("BTC", "ETH", "USDT", "USDC");

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        return bithumbFeignService.getMarketCodes().stream()
                .filter(market -> TARGET_CURRENCIES.stream()
                        .anyMatch(currency -> market.market().contains(currency)))
                .toList();
    }

    public List<BithumbVirtualAssetWarning> getVirtualAssetWarning() {
        return bithumbFeignService.getVirtualAssetWarning();
    }
}
```

**필터링 로직**:
- 주요 암호화폐만 필터링: BTC, ETH, USDT, USDC

### 3. External Service
**파일**: `src/main/java/com/yieldflow/management/global/external/bithumb/service/BithumbFeignService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbFeignService {
    private final BithumbFeignClient bithumbFeignClient;

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

### 4. Feign Client
**파일**: `src/main/java/com/yieldflow/management/global/external/bithumb/client/BithumbFeignClient.java`

```java
@FeignClient(
    name = "bithumbClient", 
    url = "${bithumb.api-url}", 
    configuration = FeignConfig.class
)
public interface BithumbFeignClient {
    @GetMapping("/v1/market/all")
    List<BithumbMarketCodeResponseDto> getMarketCodes();

    @GetMapping("v1/market/virtual_asset_warning")
    List<BithumbVirtualAssetWarning> getVirtualAssetWarning();
}
```

### 5. DTOs (Records)
**파일**: `src/main/java/com/yieldflow/management/global/external/bithumb/dto/`

```java
// 마켓 코드 응답
public record BithumbMarketCodeResponseDto(
    String market,
    String koreanName,
    String englishName,
    String marketWarning
) {}

// 가상자산 유의종목
public record BithumbVirtualAssetWarning(
    String market,
    String warningType,
    String warningMessage
) {}
```

## Feign 설정

### FeignConfig
**파일**: `src/main/java/com/yieldflow/management/global/config/FeignConfig.java`

**주요 설정**:
- JWT 인증 헤더 추가
- 타임아웃 설정
- 재시도 정책

```java
@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // JWT 토큰 추가
            String token = generateJWT();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
```

### application.yml 설정
```yaml
bithumb:
  api-url: https://api.bithumb.com
  access-key: ${BITHUMB_ACCESS_KEY}
  secret-key: ${BITHUMB_SECRET_KEY}
```

## Entity (MarketCode)
**파일**: `src/main/java/com/yieldflow/management/domain/market/entity/MarketCode.java`

```java
@Entity
@Table(name = "market_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MarketCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market", nullable = false, length = 20)
    private String market;

    @Column(name = "korean_name", length = 20)
    private String koreanName;

    @Column(name = "english_name", length = 20)
    private String englishName;

    @Column(name = "market_warning", length = 20)
    private String marketWarning;

    public void updateInfo(String koreanName, String englishName, String marketWarning) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.marketWarning = marketWarning;
    }
}
```

## Repository
**파일**: `src/main/java/com/yieldflow/management/domain/market/repository/MarketCodeRepository.java`

```java
@Repository
public interface MarketCodeRepository extends JpaRepository<MarketCode, Long> {
    Optional<MarketCode> findByMarket(String market);
    boolean existsByMarket(String market);
}
```

## 데이터베이스 스키마

```sql
CREATE TABLE IF NOT EXISTS MARKET_CODE (
    id BIGSERIAL PRIMARY KEY,
    market VARCHAR(20) NOT NULL,
    korean_name VARCHAR(20),
    english_name VARCHAR(20),
    market_warning VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

## 사용 예시

### 마켓 코드 조회
```bash
curl -X GET http://localhost:8080/api/market/market-codes \
  -H "Cookie: SESSION=..."
```

**응답 예시**:
```json
{
  "data": [
    {
      "market": "KRW-BTC",
      "koreanName": "비트코인",
      "englishName": "Bitcoin",
      "marketWarning": "NONE"
    },
    {
      "market": "KRW-ETH",
      "koreanName": "이더리움",
      "englishName": "Ethereum",
      "marketWarning": "NONE"
    }
  ]
}
```

### 가상자산 유의종목 조회
```bash
curl -X GET http://localhost:8080/api/market/virtual-asset-warning \
  -H "Cookie: SESSION=..."
```

## 구현 시 참고사항

1. **Feign Client**: OpenFeign 사용으로 간결한 HTTP 클라이언트 구현
2. **JWT 인증**: `RequestInterceptor`로 모든 요청에 자동 추가
3. **필터링**: Stream API로 특정 암호화폐만 필터링
4. **로깅**: 요청/응답 시 로그 기록
5. **BaseEntity**: `createdAt`, `updatedAt` 자동 관리
6. **Record 사용**: DTO는 불변 객체로 구현
7. **에러 처리**: Feign 에러 디코더로 예외 변환
