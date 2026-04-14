package com.insureth.insurance.service.client.flightstats;

import com.insureth.insurance.model.dto.FlightStatsRatingsResponseModel;
import com.insureth.insurance.service.client.flightstats.config.FlightStatsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "FlightStatsApiFeign",
        url = "${flightstats.api.url}",
        configuration = FlightStatsFeignConfig.class
)
public interface FlightStatsApiFeign {

    @GetMapping("/flight/{carrier}/{flightNumber}")
    FlightStatsRatingsResponseModel getFlightRatings(
            @PathVariable("carrier") String carrier,
            @PathVariable("flightNumber") String flightNumber
    );
}
