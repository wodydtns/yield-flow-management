# 인증(Authentication) 기능

## 개요
세션 기반 인증 시스템으로 로그인/로그아웃 기능을 제공합니다.

## 아키텍처

### 레이어 구조
```
Controller (AuthController)
    ↓
Service (AuthService)
    ↓
Repository (AuthRepository)
    ↓
Entity (User)
```

## 주요 컴포넌트

### 1. Controller
**파일**: `src/main/java/com/yieldflow/management/domain/auth/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest request,
            HttpServletResponse response);

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request);
}
```

**엔드포인트**:
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃

### 2. Service
**파일**: `src/main/java/com/yieldflow/management/domain/auth/service/AuthService.java`

**주요 로직**:
1. 이메일로 사용자 조회
2. 비밀번호 검증 (`PasswordEncoder.matches()`)
3. Spring Security Context 설정
4. 세션 생성 및 속성 저장
5. 세션 타임아웃 설정 (30분)

```java
public LoginResponseDto login(LoginRequestDto loginRequestDto, 
                              HttpServletRequest request,
                              HttpServletResponse response) {
    // 1. 사용자 조회
    User user = authRepository.findByEmail(loginRequestDto.email());
    
    // 2. 비밀번호 검증
    boolean matches = passwordEncoder.matches(loginRequestDto.password(), user.getPassword());
    
    // 3. SecurityContext 설정
    var authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // 4. 세션 생성
    HttpSession session = request.getSession(true);
    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    session.setMaxInactiveInterval(30 * 60);
    
    return new LoginResponseDto(...);
}
```

### 3. DTOs (Records)
**파일**: `src/main/java/com/yieldflow/management/domain/auth/dto/`

```java
// 로그인 요청
public record LoginRequestDto(
    @NotBlank @Email String email,
    String password
) {}

// 로그인 응답
public record LoginResponseDto(
    Long userId,
    String email,
    String nickname,
    UserRole role,
    UserStatus status
) {}
```

### 4. Repository
**파일**: `src/main/java/com/yieldflow/management/domain/auth/repository/AuthRepository.java`

```java
@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

## 보안 설정

### SecurityConfig
**파일**: `src/main/java/com/yieldflow/management/global/config/SecurityConfig.java`

**주요 설정**:
- CSRF 비활성화
- 세션 정책: `IF_REQUIRED`
- 인증 제외 경로: `/api/auth/**`, `/actuator/**`, `/swagger-ui/**`
- 인증 필요 경로: `/api/**` (ROLE_USER)
- 비밀번호 인코더: `BCryptPasswordEncoder`

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SECURITY_EXCLUDE_PATHS).permitAll()
            .requestMatchers("/api/**").hasRole("USER")
            .anyRequest().authenticated());
    return http.build();
}
```

## 세션 관리

### Redis 세션 저장
**설정**: `application.yml`

```yaml
spring:
  session:
    store-type: redis
    redis:
      namespace: spring:session
      flush-mode: on-save
```

## 예외 처리

### DomainException
**파일**: `src/main/java/com/yieldflow/management/global/exception/DomainException.java`

**인증 관련 예외**:
- `INVALID_EMAIL_OR_PASSWORD` - 이메일 또는 비밀번호 불일치
- `USER_NOT_FOUND` - 사용자를 찾을 수 없음
- `UNAUTHORIZED_ACCESS` - 인증되지 않은 접근

## 사용 예시

### 로그인 요청
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!"
  }'
```

### 로그아웃 요청
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Cookie: SESSION=..."
```

## 구현 시 참고사항

1. **비밀번호 검증**: `PasswordEncoder`를 반드시 사용
2. **세션 타임아웃**: 기본 30분 설정
3. **SecurityContext**: 세션에 저장하여 인증 상태 유지
4. **예외 처리**: `DomainException`으로 통일된 예외 처리
5. **응답 형식**: `ApiResponse<T>` 사용
