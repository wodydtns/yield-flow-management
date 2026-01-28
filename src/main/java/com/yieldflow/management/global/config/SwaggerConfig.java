package com.yieldflow.management.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Value("${spring.application.name}")
        private String applicationName;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("https://wodydtns.duckdns.org")
                                                                .description("Production Server")))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
        }

        private Info apiInfo() {
                return new Info()
                                .title("YieldFlow Management API")
                                .description("API documentation for YieldFlow Management System ")
                                .version("v1.0.0")
                                .contact(new Contact()
                                                .name("박재욱")
                                                .email("wodydtns@gmail.com"))
                                .license(new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT"));
        }

}
