# 사용자 관리(User Management) 기능

## 개요
사용자 생성 및 이메일 인증 관리 기능을 제공합니다.

## 아키텍처

### 레이어 구조
```
Controller (UserController)
    ↓
Service (UserService)
    ↓
Repository (UserRepository)
    ↓
Entity (User)
```

## 주요 컴포넌트

### 1. Controller
**파일**: `src/main/java/com/yieldflow/management/domain/user/controller/UserController.java`

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final BithumbFeignService bithumbFeignService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createUser(@Valid @RequestBody UserRequestDto userRequestDto);

    @GetMapping("/bithumb-account")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BithumbAccountResponseDto>> getUserBithumbAccount();
}
```

**엔드포인트**:
- `POST /api/users` - 사용자 생성
- `GET /api/users/bithumb-account` - 빗썸 계정 조회

### 2. Service
**파일**: `src/main/java/com/yieldflow/management/domain/user/service/UserService.java`

**주요 로직**:
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRequestDto userRequestDto) {
        // 1. 중복 사용자 검증
        User user = userRepository.findByEmail(userRequestDto.email());
        if (user != null) {
            throw new DomainException(DomainExceptionCode.USER_ALREADY_EXISTS);
        }
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRequestDto.password());
        
        // 3. 사용자 저장
        userRepository.save(userRequestDto.createUser(encodedPassword));
    }

    public void updateVerified(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new DomainException(DomainExceptionCode.USER_NOT_FOUND);
        }
        user.setVerified();
        userRepository.save(user);
    }
}
```

### 3. Entity
**파일**: `src/main/java/com/yieldflow/management/domain/user/entity/User.java`

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;

    @Column(nullable = false)
    private boolean isVerified;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<ApiKey> apiKeys = new ArrayList<>();

    @Builder
    public User(String email, String password, String nickname, 
                UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.isVerified = false;
    }

    public void setVerified() {
        this.isVerified = true;
    }
}
```

### 4. DTO (Record)
**파일**: `src/main/java/com/yieldflow/management/domain/user/dto/UserRequestDto.java`

```java
public record UserRequestDto(
    @NotBlank @Email 
    String email,
    
    @NotBlank 
    @Size(min = 8, max = 16, message = "비밀번호(password)는 8자리 이상 16자리 이하로 입력해야 합니다.") 
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':,.<>/?]{8,16}$", 
             message = "비밀번호(password)는 대문자, 숫자, 특수문자를 포함해야 합니다.") 
    String password,
    
    @NotBlank 
    String nickname
) {
    public User createUser(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
```

**비밀번호 검증 규칙**:
- 길이: 8~16자
- 대문자 1개 이상
- 숫자 1개 이상
- 특수문자 1개 이상

### 5. Repository
**파일**: `src/main/java/com/yieldflow/management/domain/user/repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

## Enums

### UserRole
```java
public enum UserRole {
    USER,
    ADMIN
}
```

### UserStatus
```java
public enum UserStatus {
    ACTIVE,
    BANNED,
    WITHDRAWN
}
```

## 데이터베이스 스키마

```sql
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255),
    nickname      VARCHAR(50) NOT NULL,
    profile_image VARCHAR(500),
    role          VARCHAR(20) DEFAULT 'USER',
    status        VARCHAR(20) DEFAULT 'ACTIVE',
    is_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);
```

## 사용 예시

### 사용자 생성
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!",
    "nickname": "홍길동"
  }'
```

### 빗썸 계정 조회
```bash
curl -X GET http://localhost:8080/api/users/bithumb-account \
  -H "Cookie: SESSION=..."
```

## 구현 시 참고사항

1. **비밀번호 암호화**: `BCryptPasswordEncoder` 사용 필수
2. **이메일 중복 검증**: 사용자 생성 전 반드시 확인
3. **기본값 설정**: `role=USER`, `status=ACTIVE`, `isVerified=false`
4. **Builder 패턴**: 생성자 대신 Builder 사용
5. **Validation**: Jakarta Validation (`@Valid`, `@NotBlank`, `@Email` 등) 활용
6. **예외 처리**: `DomainException` 사용
7. **연관관계**: `SocialAccount`, `ApiKey`와 1:N 관계
