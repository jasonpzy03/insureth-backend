package com.insureth.insurance.service.client.airlabs;

import com.insureth.insurance.service.client.airlabs.config.AirLabsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "AirLabsApiFeign",
        url = "${airlabs.api.url}",
        configuration = AirLabsFeignConfig.class)
public interface AirLabsApiFeign {

}