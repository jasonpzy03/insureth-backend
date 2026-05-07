package com.insureth.insurance.service.client.aviationstack;

import com.insureth.insurance.model.dto.AviationStackFlightScheduleResponseModel;
import com.insureth.insurance.service.client.aviationstack.config.AviationStackFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(
        name = "AviationStackApiFeign",
        url = "${aviationStack.api.url}",
        configuration = AviationStackFeignConfig.class)
public interface AviationStackApiFeign {
    @GetMapping("/flightsFuture")
    AviationStackFlightScheduleResponseModel getFlightFutureSchedule(@RequestParam("airline_iata") String airlineIATACode,
                                                                     @RequestParam("flight_number") String flightNumber,
                                                                     @RequestParam("date")
                                                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                                     @RequestParam("iataCode") String airportIATACode,
                                                                     @RequestParam("type") String type);
}
