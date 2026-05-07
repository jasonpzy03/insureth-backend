package com.insureth.insurance.service.client.auth.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AuthUserFeignConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api.key}") String internalApiKey
    ) {
        return requestTemplate -> requestTemplate.header("X-Internal-Api-Key", internalApiKey);
    }
}
