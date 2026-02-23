package com.yieldflow.management.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;

@Configuration
public class BinanceFeignConfig {

    @Value("${binance.api-key}")
    private String accessKey;

    @Value("${binance.api-secret}")
    private String secretKey;

    @Bean
    public Logger.Level binanceFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
