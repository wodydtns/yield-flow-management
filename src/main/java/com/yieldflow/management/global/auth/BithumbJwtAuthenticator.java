package com.yieldflow.management.global.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Bithumb API JWT 인증 유틸리티
 */
@Slf4j
@Component
public class BithumbJwtAuthenticator {

    @Value("${bithumb.api.access-key:}")
    private String accessKey;

    @Value("${bithumb.api.secret-key:}")
    private String secretKey;

    /**
     * Bithumb API 호출을 위한 JWT 토큰 생성
     * 
     * @return Bearer 토큰 형식의 인증 헤더 값
     */
    public String generateAuthToken() {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            throw new IllegalStateException(
                    "Bithumb API credentials not configured. Please set BITHUMB_ACCESS_KEY and BITHUMB_SECRET_KEY environment variables");
        }

        var algorithm = Algorithm.HMAC256(secretKey);
        var jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .sign(algorithm);

        var authenticationToken = "Bearer " + jwtToken;
        log.debug("Generated JWT token for Bithumb API authentication");

        return authenticationToken;
    }

    /**
     * API 키 설정 여부 확인
     * 
     * @return API 키가 설정되어 있으면 true
     */
    public boolean isConfigured() {
        return !accessKey.isEmpty() && !secretKey.isEmpty();
    }
}
