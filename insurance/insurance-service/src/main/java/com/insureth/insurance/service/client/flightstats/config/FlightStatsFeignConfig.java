package com.insureth.insurance.service.client.flightstats.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FlightStatsFeignConfig {

    private static final String APP_ID = "4c347e23";
    private static final String APP_KEY = "00bf5b2f286d1b96767b25e0a5176daa";

    @Bean
    public RequestInterceptor flightStatsCredentialsInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("appId", APP_ID);
            requestTemplate.query("appKey", APP_KEY);
        };
    }
}
