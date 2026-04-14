package com.insureth.insurance.service.client.aviationstack.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AviationStackFeignConfig {
    @Value("${aviationstack.api.key}")
    private String accessKey;

    @Bean
    public RequestInterceptor accessKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("access_key", accessKey);
        };
    }
}
