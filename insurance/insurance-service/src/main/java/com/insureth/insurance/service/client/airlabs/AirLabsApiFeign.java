package com.insureth.insurance.service.client.airlabs;

import com.insureth.insurance.model.dto.AirLabsAirlineResponse;
import com.insureth.insurance.model.dto.AirLabsAirportResponse;
import com.insureth.insurance.model.dto.AirLabsScheduleResponse;
import com.insureth.insurance.model.dto.AirLabsTimezoneResponse;
import com.insureth.insurance.service.client.airlabs.config.AirLabsFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "AirLabsApiFeign",
        url = "${airlabs.api.url}",
        configuration = AirLabsFeignConfig.class)
public interface AirLabsApiFeign {

    @GetMapping("/airports")
    AirLabsAirportResponse getAirports(@RequestParam("_fields") String fields);

    @GetMapping("/airlines")
    AirLabsAirlineResponse getAirlines(@RequestParam("_fields") String fields);

    @GetMapping("/timezones")
    AirLabsTimezoneResponse getTimezones();

    @GetMapping("/schedules")
    AirLabsScheduleResponse getSchedules(
            @RequestParam("flight_iata") String flightIata,
            @RequestParam("_fields") String fields
    );

    @GetMapping("/delays")
    AirLabsScheduleResponse getDelays(
            @RequestParam("delay") int delay,
            @RequestParam("type") String type,
            @RequestParam("flight_iata") String flightIata,
            @RequestParam("_fields") String fields
    );
}
