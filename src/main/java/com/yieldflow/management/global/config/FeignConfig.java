package com.yieldflow.management.global.config;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${bithumb.access-key}")
    private String accessKey;

    @Value("${bithumb.secret-key}")
    private String secretKey;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Bithumb API JWT 인증 Interceptor
     * 모든 Feign 요청에 JWT 토큰을 자동으로 추가합니다.
     */
    @Bean
    public RequestInterceptor bithumbAuthInterceptor() {
        return requestTemplate -> {
            try {
                String jwtToken = generateBithumbToken(requestTemplate);
                requestTemplate.header("Authorization", jwtToken);
                requestTemplate.header("Content-Type", "application/json");
                requestTemplate.header("Accept", "application/json");
            } catch (Exception e) {
                log.error("Failed to generate Bithumb JWT token", e);
                throw new RuntimeException("Failed to generate Bithumb JWT token", e);
            }
        };
    }

    /**
     * Bithumb API용 JWT 토큰 생성
     * Query 파라미터가 있는 경우 query_hash를 포함합니다.
     */
    private String generateBithumbToken(RequestTemplate template) throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        var jwtBuilder = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis());

        // Query 파라미터가 있는 경우 query_hash 추가
        String queryString = template.queryLine();
        if (queryString != null && !queryString.isEmpty()) {
            if (queryString.startsWith("?")) {
                queryString = queryString.substring(1);
            }

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(queryString.getBytes(StandardCharsets.UTF_8));
            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            jwtBuilder.withClaim("query_hash", queryHash);
            jwtBuilder.withClaim("query_hash_alg", "SHA512");

            log.debug("Generated query_hash for query: {}", queryString);
        }

        String token = jwtBuilder.sign(algorithm);
        return "Bearer " + token;
    }

    /**
     * 재시도 설정 (Retryer)
     * 기본값은 재시도 하지 않음(Retryer.NEVER_RETRY)
     * 아래 예시는 1초 간격으로 시작해 최대 2초까지 늘어나며, 최대 3번 재시도
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3);
    }
}
