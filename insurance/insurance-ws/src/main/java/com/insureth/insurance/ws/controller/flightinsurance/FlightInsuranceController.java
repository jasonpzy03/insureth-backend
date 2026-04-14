package com.insureth.insurance.ws.controller.flightinsurance;

import com.insureth.insurance.model.dto.AirlineResponseModel;
import com.insureth.insurance.model.dto.AirportResponseModel;
import com.insureth.insurance.model.dto.FlightInsuranceExperienceConfigResponse;
import com.insureth.insurance.model.dto.FlightInsuranceQuoteResponse;
import com.insureth.insurance.model.dto.FlightScheduleResponseModel;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceConfigService;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceService;
import com.insureth.insurance.ws.api.FlightInsuranceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/insurance/flightinsurance")
@Slf4j
@RequiredArgsConstructor
public class FlightInsuranceController implements FlightInsuranceApi {
    private final FlightInsuranceService flightInsuranceService;
    private final FlightInsuranceConfigService flightInsuranceConfigService;

    @Override
    public ResponseEntity<FlightScheduleResponseModel> getFlightFutureSchedule(@RequestParam String airlineIATACode,
                                                                               @RequestParam String flightNumber,
                                                                               @RequestParam String departureAirportIATACode,
                                                                               @RequestParam LocalDate departureDate) {
        return ResponseEntity.ok(flightInsuranceService.getFlightFutureSchedule(
                airlineIATACode,
                flightNumber,
                departureAirportIATACode,
                departureDate
                ));
    }

    @Override
    public ResponseEntity<List<AirlineResponseModel>> getAirlines() {
        List<AirlineResponseModel> airlines = flightInsuranceConfigService.getSupportedAirlines();
        return ResponseEntity.ok(airlines);
    }

    @Override
    public ResponseEntity<List<AirportResponseModel>> getAirports() {
        List<AirportResponseModel> airports = flightInsuranceConfigService.getSupportedAirports();
        return ResponseEntity.ok(airports);
    }

    @GetMapping("/config")
    public ResponseEntity<FlightInsuranceExperienceConfigResponse> getConfig() {
        return ResponseEntity.ok(flightInsuranceConfigService.getPublicConfig());
    }

    @GetMapping("/quote")
    public ResponseEntity<FlightInsuranceQuoteResponse> getQuote(
            @RequestParam String airlineIATACode,
            @RequestParam String flightNumber,
            @RequestParam LocalDate departureDate
    ) {
        return ResponseEntity.ok(
                flightInsuranceService.getQuote(airlineIATACode, flightNumber, departureDate)
        );
    }
}
