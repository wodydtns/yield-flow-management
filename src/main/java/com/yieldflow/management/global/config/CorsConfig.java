package com.yieldflow.management.global.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    config.setAllowedHeaders(Arrays.asList(
        "Origin",
        "Accept",
        "X-Requested-With",
        "X-License-Key",
        "Content-Type",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers",
        "Authorization",
        "X-CSRF-TOKEN",
        "sec-ch-ua",
        "sec-ch-ua-mobile",
        "sec-ch-ua-platform"));
    config.setExposedHeaders(Arrays.asList(
        "Authorization",
        "X-License-Key",
        "X-CSRF-TOKEN",
        "xsrf-token"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
