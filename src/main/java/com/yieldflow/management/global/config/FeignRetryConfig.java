package com.yieldflow.management.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Retryer;

@Configuration
public class FeignRetryConfig {

    /**
     * 공용 Feign Retryer 설정: 0.5초 시작, 2초 최대 간격, 최대 3회 재시도.
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(500, 2000, 3);
    }
}
