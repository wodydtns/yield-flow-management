-- 1. Users 테이블 (회원 기본 정보)
CREATE TABLE users (
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

-- Users 테이블 및 컬럼 주석
COMMENT ON TABLE users IS '회원 기본 정보';
COMMENT ON COLUMN users.email IS '로그인 ID 역할';
COMMENT ON COLUMN users.password IS '비밀번호 (소셜 로그인 유저는 NULL)';
COMMENT ON COLUMN users.nickname IS '사용자 닉네임';
COMMENT ON COLUMN users.profile_image IS '프로필 이미지 URL';
COMMENT ON COLUMN users.role IS '권한 (USER, ADMIN)';
COMMENT ON COLUMN users.status IS '상태 (ACTIVE, BANNED, WITHDRAWN)';
COMMENT ON COLUMN users.is_verified IS '이메일 인증 여부';


-- 2. Social Accounts 테이블 (소셜 연동 정보)
CREATE TABLE social_accounts (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    provider      VARCHAR(20) NOT NULL,
    provider_id   VARCHAR(255) NOT NULL,
    connected_at  TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_social_provider UNIQUE (provider, provider_id)
);

-- Social Accounts 테이블 및 컬럼 주석
COMMENT ON TABLE social_accounts IS '소셜 로그인 연동 정보';
COMMENT ON COLUMN social_accounts.provider IS '소셜 제공자 (예: google, kakao, naver)';
COMMENT ON COLUMN social_accounts.provider_id IS '소셜 서비스의 고유 식별값 (sub, id 등)';
COMMENT ON COLUMN social_accounts.connected_at IS '연동된 일시';


-- 3. API Keys 테이블 (API 인증 키 관리)
CREATE TABLE api_keys (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    name          VARCHAR(50) NOT NULL,
    access_key    VARCHAR(64) UNIQUE,
    secret_key    VARCHAR(255) UNIQUE,
    is_active     BOOLEAN DEFAULT TRUE,
    last_used_at  TIMESTAMPTZ,
    expires_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_apikey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- API Keys 테이블 및 컬럼 주석
COMMENT ON TABLE api_keys IS '사용자 API 접근 키 관리';
COMMENT ON COLUMN api_keys.name IS '사용자가 키를 구분하기 위한 별칭 (예: Trading Bot 1)';
COMMENT ON COLUMN api_keys.access_key IS '공개 식별자 (API 호출 시 Header에 포함)';
COMMENT ON COLUMN api_keys.secret_key IS '비밀 키';
COMMENT ON COLUMN api_keys.is_active IS '키 활성화 여부';
COMMENT ON COLUMN api_keys.last_used_at IS '마지막 사용 시점 (보안 감사용)';
COMMENT ON COLUMN api_keys.expires_at IS '키 만료일 (NULL이면 무제한)';
