---
# Auto-Trade Architecture Overview
---

## Purpose
요약: 현행 프로젝트는 Bithumb REST API와 연동해 계좌 조회, 주문 가능 정보, 마켓 코드, 가상자산 경고를 조회하는 관리 백엔드입니다. 자동매매 봇이 재사용할 수 있도록 핵심 구성과 연동 포인트를 정리합니다.

## 사용 기술 (In Use)
- **언어/플랫폼**: Java 21, Spring Boot 3.5.x (Tomcat, Servlet stack, Virtual Threads 전제)
- **통신/외부연동**: Spring Cloud OpenFeign + FeignConfig(JWT 서명, 재시도, FULL 로깅)
- **데이터/세션**: PostgreSQL(JPA/Hibernate), Redis(Spring Session, Cache), H2(테스트)
- **메시징**: RabbitMQ 템플릿/리스너 설정 포함
- **보안/검증**: Spring Security, Bean Validation
- **문서화/관측**: Springdoc OpenAPI 2.x, Actuator (health/metrics/prometheus), Feign Logger
- **View/메일**: Thymeleaf, Spring Mail

## 주요 구성요소
- **Feign 기반 외부 연동**
  - `BithumbFeignClient`가 Bithumb API 엔드포인트(/v1/accounts, /v1/orders/chance, /v1/market/all, /v1/market/virtual_asset_warning)를 정의합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\external\bithumb\client\BithumbFeignClient.java#14-36)
  - `BithumbFeignService`가 클라이언트를 감싸며 로깅과 간단한 전달 책임을 집니다. 자동매매 봇은 이 서비스를 통해 계좌/마켓/주문가능 정보 등을 조회할 수 있습니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\external\bithumb\service\BithumbFeignService.java#21-58)
  - 공통 설정은 `FeignConfig`에서 JWT 토큰 생성, 헤더 주입, 로깅 레벨, 재시도 정책을 관리합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\config\FeignConfig.java#25-96)
- **주문 가능 정보 API**
  - `OrderController` `/api/orders/chance` 엔드포인트가 존재하며, 현재는 Feign 서비스에 위임합니다. 자동매매 봇이 사전 체크로 활용 가능합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\domain\order\controller\OrderController.java#21-29)
- **도메인 DTO (records)**
  - 주문 가능 응답: `OrderChanceResponseDto`와 중첩 record로 시장/계정 정보를 모델링합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\domain\order\dto\OrderChanceResponseDto.java#6-40)
  - 계좌/마켓 코드/가상자산 경고 DTO: `BithumbAccountResponseDto`, `BithumbMarketCodeResponseDto`, `BithumbVirtualAssetWarning`(파일 경로 상 DTO 목록) 등이 Feign 응답을 캡슐화합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\external\bithumb\dto\BithumbAccountResponseDto.java#3-10, @d:\yieldflow-management\src\main\java\com\yieldflow\management\global\external\bithumb\dto\BithumbMarketCodeResponseDto.java#3-10)

## 보안/환경 변수
- `application.yml`에 Bithumb `api-url`, `access-key`, `secret-key`가 정의되어 있습니다. 운영/개발 키는 환경 변수로 주입하고, 커밋에서는 비워두거나 안전한 값으로 교체해야 합니다. (@d:\yieldflow-management\src\main\resources\application.yml#169-172)

## 자동매매 봇 연동 가이드
1. **주문 전략 입력**: 전략 모듈에서 시장 코드(KRW-BTC 등) 결정 후 `/v1/orders/chance`를 호출해 최소/수수료/허용 타입을 확인합니다 (`BithumbFeignService#getOrderChance`).
2. **계좌 상태 조회**: 포지션/잔고를 위해 `getAccounts()` 사용.
3. **거래 가능 여부 필터링**: `getMarketCodes()` 및 `getVirtualAssetWarning()`로 거래 제한/경고 마켓을 필터.
4. **주문 실행 구현**: 현재 코드에는 주문 실행 API 호출이 없으므로, Feign 클라이언트에 Bithumb의 주문 엔드포인트를 추가하고 JWT 서명(쿼리 해시 포함)을 재사용합니다. `FeignConfig`의 `generateBithumbToken` 로직을 그대로 활용하십시오. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\config\FeignConfig.java#59-86)
5. **모니터링/로깅**: Feign 로깅 레벨은 FULL로 설정되어 있어 호출 트레이스를 확보할 수 있습니다. 필요 시 actuator + prometheus 지표를 수집합니다. (@d:\yieldflow-management\src\main\java\com\yieldflow\management\global\config\FeignConfig.java#31-35)

## 확장 아이디어 (자동매매 봇용)
- **주문/체결 기록 도메인 추가**: 체결 내역 저장을 위한 엔티티와 저장소, 서비스 계층을 추가해 전략 백테스트/리플레이에 활용.
- **스케줄러/배치**: Spring 스케줄러 또는 외부 워크플로우로 주기적 시그널 계산과 주문 실행을 트리거. 현재 코드에는 스케줄러가 없으므로 별도 모듈로 추가 필요.
- **리스크 관리**: 1) 시장 경고/거래중단 마켓 필터, 2) 최대 주문 금액/호가 제한, 3) 슬리피지/미체결 관리 로직을 서비스 계층에 배치.
- **백오프/재시도 정책**: `Retryer` 기본 설정(최대 3회, 1~2초 백오프)을 전략적으로 조정하거나, 특정 에러 코드별 맞춤 재시도/중단 정책을 추가.

## 빠른 체크리스트 (운영 준비)
- [ ] Bithumb API 키를 환경 변수/비밀 저장소로 분리
- [ ] 주문 실행 API (매수/매도) Feign 메서드 추가 및 통합 테스트
- [ ] 스케줄러/전략 모듈에서 계좌-주문-체결 흐름 종단 테스트
- [ ] 관측성: actuator + prometheus + 중앙 로그 수집 활성화
