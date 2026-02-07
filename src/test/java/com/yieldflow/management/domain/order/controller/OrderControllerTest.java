package com.yieldflow.management.domain.order.controller;

import com.yieldflow.management.domain.order.dto.OrderChanceResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;

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

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = FeignAutoConfiguration.class)
@TestPropertySource(properties = "bithumb.api-url=http://localhost")
class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private BithumbFeignService bithumbFeignService;

        @Test
        @DisplayName("GET /api/orders/chance - 기본 마켓(KRW-BTC) 주문 가능 정보 조회 성공")
        void getOrderChance_defaultMarket_success() throws Exception {
                // Given
                var market = new OrderChanceResponseDto.Market(
                                "KRW-BTC", "Bitcoin", List.of("limit"), List.of("ask", "bid"),
                                List.of("limit"), List.of("limit"),
                                new OrderChanceResponseDto.MarketSupport("KRW", "1000", "5000"),
                                new OrderChanceResponseDto.MarketSupport("BTC", "0.00000001", "5000"),
                                "1000000000", "active");
                var bidAccount = new OrderChanceResponseDto.Account("KRW", "1000000", "0", "0", false, "KRW");
                var askAccount = new OrderChanceResponseDto.Account("BTC", "0.5", "0", "50000000", false, "KRW");
                var response = new OrderChanceResponseDto("0.0025", "0.0025", "0.0015", "0.0015", market, bidAccount,
                                askAccount);

                given(bithumbFeignService.getOrderChance("KRW-BTC")).willReturn(response);

                // When & Then
                mockMvc.perform(get("/api/orders/chance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.bid_fee").value("0.0025"))
                                .andExpect(jsonPath("$.data.ask_fee").value("0.0025"))
                                .andExpect(jsonPath("$.data.market.id").value("KRW-BTC"))
                                .andExpect(jsonPath("$.data.bid_account.currency").value("KRW"))
                                .andExpect(jsonPath("$.data.ask_account.currency").value("BTC"));
        }

        @Test
        @DisplayName("GET /api/orders/chance?market=KRW-ETH - 특정 마켓 주문 가능 정보 조회 성공")
        void getOrderChance_specificMarket_success() throws Exception {
                // Given
                var market = new OrderChanceResponseDto.Market(
                                "KRW-ETH", "Ethereum", List.of("limit"), List.of("ask", "bid"),
                                List.of("limit"), List.of("limit"),
                                new OrderChanceResponseDto.MarketSupport("KRW", "1000", "5000"),
                                new OrderChanceResponseDto.MarketSupport("ETH", "0.00000001", "5000"),
                                "1000000000", "active");
                var bidAccount = new OrderChanceResponseDto.Account("KRW", "500000", "0", "0", false, "KRW");
                var askAccount = new OrderChanceResponseDto.Account("ETH", "1.0", "0", "3000000", false, "KRW");
                var response = new OrderChanceResponseDto("0.0025", "0.0025", "0.0015", "0.0015", market, bidAccount,
                                askAccount);

                given(bithumbFeignService.getOrderChance("KRW-ETH")).willReturn(response);

                // When & Then
                mockMvc.perform(get("/api/orders/chance").param("market", "KRW-ETH"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.market.id").value("KRW-ETH"))
                                .andExpect(jsonPath("$.data.bid_account.currency").value("KRW"))
                                .andExpect(jsonPath("$.data.ask_account.currency").value("ETH"));
        }
}
