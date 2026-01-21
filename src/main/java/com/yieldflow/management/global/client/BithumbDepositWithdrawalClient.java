package com.yieldflow.management.global.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yieldflow.management.global.auth.BithumbJwtAuthenticator;
import com.yieldflow.management.global.client.dto.BithumbDepositWithdrawalResponse;
import com.yieldflow.management.global.exception.BithumbApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 빗썸 입출금 현황 조회 API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BithumbDepositWithdrawalClient {

    private final BithumbJwtAuthenticator jwtAuthenticator;
    private final ObjectMapper objectMapper;

    @Value("${bithumb.api.base-url:https://api.bithumb.com}")
    private String apiBaseUrl;

    /**
     * 입출금 현황 조회
     * 
     * @param currency 통화 코드 (BTC, ETH 등, 전체 조회시 "ALL")
     * @param searchGb 조회 구분 (0: 전체, 1: 입금, 2: 출금)
     * @param count    조회 개수 (기본값: 20, 최대: 1000)
     * @return 입출금 현황 정보
     */
    public BithumbDepositWithdrawalResponse getDepositWithdrawalStatus(String currency, Integer searchGb,
            Integer count) {
        if (!jwtAuthenticator.isConfigured()) {
            throw new BithumbApiException("Bithumb API credentials not configured");
        }

        var endpoint = String.format("/info/user_transactions?currency=%s&searchGb=%d&count=%d",
                currency != null ? currency : "ALL",
                searchGb != null ? searchGb : 0,
                count != null ? count : 20);

        var url = apiBaseUrl + endpoint;
        var authenticationToken = jwtAuthenticator.generateAuthToken();

        log.debug("Fetching deposit/withdrawal status for currency: {}, searchGb: {}, count: {}", currency, searchGb,
                count);

        try (var client = HttpClients.createDefault()) {
            var httpRequest = new HttpGet(url);
            httpRequest.addHeader("Authorization", authenticationToken);

            try (var response = client.execute(httpRequest)) {
                var httpStatus = response.getCode();
                var responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                log.debug("API Response - Status: {}, Body: {}", httpStatus, responseBody);

                if (httpStatus != 200) {
                    log.error("API call failed with status: {}, body: {}", httpStatus, responseBody);
                    throw new BithumbApiException(
                            "API call failed with status: " + httpStatus + ", body: " + responseBody);
                }

                return objectMapper.readValue(responseBody, BithumbDepositWithdrawalResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to execute deposit/withdrawal status API call: {}", e.getMessage());
            throw new BithumbApiException("Failed to execute API call", e);
        }
    }

    /**
     * 전체 입출금 현황 조회 (기본 파라미터)
     * 
     * @return 전체 입출금 현황 정보
     */
    public BithumbDepositWithdrawalResponse getAllDepositWithdrawalStatus() {
        return getDepositWithdrawalStatus("ALL", 0, 20);
    }

    /**
     * 입금 현황만 조회
     * 
     * @param currency 통화 코드
     * @param count    조회 개수
     * @return 입금 현황 정보
     */
    public BithumbDepositWithdrawalResponse getDepositStatus(String currency, Integer count) {
        return getDepositWithdrawalStatus(currency, 1, count);
    }

    /**
     * 출금 현황만 조회
     * 
     * @param currency 통화 코드
     * @param count    조회 개수
     * @return 출금 현황 정보
     */
    public BithumbDepositWithdrawalResponse getWithdrawalStatus(String currency, Integer count) {
        return getDepositWithdrawalStatus(currency, 2, count);
    }

    /**
     * 지갑 상태 조회 (예시 API와 동일한 엔드포인트)
     * 
     * @return 지갑 상태 정보 (JSON 문자열)
     */
    public String getWalletStatus() {
        if (!jwtAuthenticator.isConfigured()) {
            throw new BithumbApiException("Bithumb API credentials not configured");
        }

        var url = apiBaseUrl + "/v1/status/wallet";
        var authenticationToken = jwtAuthenticator.generateAuthToken();

        log.debug("Fetching wallet status");

        try (var client = HttpClients.createDefault()) {
            var httpRequest = new HttpGet(url);
            httpRequest.addHeader("Authorization", authenticationToken);

            try (var response = client.execute(httpRequest)) {
                var httpStatus = response.getCode();
                var responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                log.debug("Wallet Status API Response - Status: {}, Body: {}", httpStatus, responseBody);

                if (httpStatus != 200) {
                    log.error("Wallet status API call failed with status: {}, body: {}", httpStatus, responseBody);
                    throw new BithumbApiException("Wallet status API call failed with status: " + httpStatus);
                }

                return responseBody;
            }
        } catch (Exception e) {
            log.error("Failed to execute wallet status API call: {}", e.getMessage());
            throw new BithumbApiException("Failed to execute wallet status API call", e);
        }
    }
}
