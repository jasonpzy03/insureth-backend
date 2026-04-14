package com.insureth.insurance.service.client.airlabs.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AirLabsFeignConfig {

    @Value("${airlabs.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("api_key", apiKey);
        };
    }
}