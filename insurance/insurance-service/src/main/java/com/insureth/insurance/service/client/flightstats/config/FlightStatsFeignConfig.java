package com.insureth.insurance.service.client.flightstats.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FlightStatsFeignConfig {

    @Value("${flightstats.api.appId}")
    private String APP_ID;

    @Value("${flightstats.api.appKey}")
    private String APP_KEY;

    @Bean
    public RequestInterceptor flightStatsCredentialsInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("appId", APP_ID);
            requestTemplate.query("appKey", APP_KEY);
        };
    }
}
