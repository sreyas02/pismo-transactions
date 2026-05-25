package com.pismo.transactions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pismoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pismo Transactions API")
                        .description("Financial transactions service — Accounts and Transactions")
                        .version("1.0.0")
                        );
    }
}