package com.yieldflow.management.global.external.bithumb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbAccountResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbService {

    @Value("${bithumb.api-url}")
    private String apiUrl;

    @Value("${bithumb.access-key}")
    private String accessKey;

    @Value("${bithumb.secret-key}")
    private String secretKey;

    private final RestClient restClient = RestClient.create();

    /**
     * 계좌 조회 (GET /v1/accounts)
     */
    public List<BithumbAccountResponseDto> getAccounts() {
        String jwtToken = generateToken(Map.of());

        List<BithumbAccountResponseDto> responseDto = restClient.get()
                .uri(apiUrl + "/v1/accounts")
                .header("Authorization", jwtToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<BithumbAccountResponseDto>>() {
                });

        log.info("responseDto: {}", responseDto);
        return responseDto;
    }

    /**
     * 주문 가능 정보 조회 (GET /v1/orders/chance)
     */
    public OrderChanceResponseDto getOrderChance(String market) {
        try {
            // 1. Query 파라미터 설정
            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair("market", market));

            // 2. Query 문자열 생성 및 해시
            String query = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(query.getBytes(StandardCharsets.UTF_8));
            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            String jwtToken = generateToken(Map.of(
                    "query_hash", queryHash,
                    "query_hash_alg", "SHA512"));

            OrderChanceResponseDto responseDto = restClient.get()
                    .uri(apiUrl + "/v1/orders/chance?" + query)
                    .header("Authorization", jwtToken)
                    .retrieve()
                    .body(OrderChanceResponseDto.class);

            log.info("OrderChance responseDto: {}", responseDto);
            return responseDto;
        } catch (Exception e) {
            log.error("Failed to get order chance for market: {}", market, e);
            throw new RuntimeException("Failed to get order chance", e);
        }
    }

    /**
     * JWT 토큰 생성 로직
     * 
     * @param additionalClaims API 호출마다 추가할 claim (예: query_hash, query 등)
     */
    private String generateToken(Map<String, Object> additionalClaims) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        var jwtBuilder = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis());

        // 추가 claim이 있으면 동적으로 추가
        additionalClaims.forEach((key, value) -> {
            if (value instanceof String) {
                jwtBuilder.withClaim(key, (String) value);
            } else if (value instanceof Integer) {
                jwtBuilder.withClaim(key, (Integer) value);
            } else if (value instanceof Long) {
                jwtBuilder.withClaim(key, (Long) value);
            } else if (value instanceof Boolean) {
                jwtBuilder.withClaim(key, (Boolean) value);
            } else {
                jwtBuilder.withClaim(key, value.toString());
            }
        });

        String token = jwtBuilder.sign(algorithm);
        return "Bearer " + token;
    }
}
