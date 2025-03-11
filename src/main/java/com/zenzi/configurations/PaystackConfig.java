package com.zenzi.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Configuration
public class PaystackConfig {

    @Value("${paystack.secret.key}")
    private String secretKey;

    @Value("${paystack.base.url}")
    private String baseUrl;


    @Bean
    public WebClient getWebClientBuilder() {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}

