package com.yieldflow.management.domain.market.controller;

import com.yieldflow.management.domain.market.service.MarketService;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbVirtualAssetWarning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = FeignAutoConfiguration.class)
@TestPropertySource(properties = "bithumb.api-url=http://localhost")
class MarketControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private MarketService marketService;

        @Test
        @DisplayName("GET /api/market/market-codes - 마켓 코드 조회 성공")
        void getMarketCodes_success() throws Exception {
                // Given
                var marketCodes = List.of(
                                new BithumbMarketCodeResponseDto("KRW-BTC", "비트코인", "Bitcoin", "NONE"),
                                new BithumbMarketCodeResponseDto("KRW-ETH", "이더리움", "Ethereum", "NONE"));
                given(marketService.getMarketCodes()).willReturn(marketCodes);

                // When & Then
                mockMvc.perform(get("/api/market/market-codes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].market").value("KRW-BTC"))
                                .andExpect(jsonPath("$.data[0].koreanName").value("비트코인"))
                                .andExpect(jsonPath("$.data[1].market").value("KRW-ETH"));
        }

        @Test
        @DisplayName("GET /api/market/market-codes - 빈 목록 반환")
        void getMarketCodes_emptyList() throws Exception {
                // Given
                given(marketService.getMarketCodes()).willReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/market/market-codes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /api/market/virtual-asset-warning - 가상자산 경고 조회 성공")
        void getVirtualAssetWarning_success() throws Exception {
                // Given
                var warnings = List.of(
                                new BithumbVirtualAssetWarning("KRW-XRP", "CAUTION", "1", "2026-12-31"),
                                new BithumbVirtualAssetWarning("KRW-DOGE", "CAUTION", "2", "2026-06-30"));
                given(marketService.getVirtualAssetWarning()).willReturn(warnings);

                // When & Then
                mockMvc.perform(get("/api/market/virtual-asset-warning"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].market").value("KRW-XRP"))
                                .andExpect(jsonPath("$.data[0].warningType").value("CAUTION"))
                                .andExpect(jsonPath("$.data[1].market").value("KRW-DOGE"));
        }

        @Test
        @DisplayName("GET /api/market/virtual-asset-warning - 빈 경고 목록 반환")
        void getVirtualAssetWarning_emptyList() throws Exception {
                // Given
                given(marketService.getVirtualAssetWarning()).willReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/market/virtual-asset-warning"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));
        }
}
