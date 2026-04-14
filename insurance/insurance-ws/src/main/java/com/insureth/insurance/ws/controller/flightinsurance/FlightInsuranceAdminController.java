package com.insureth.insurance.ws.controller.flightinsurance;

import com.insureth.insurance.model.dto.AdminPagedResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirlineModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirportModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAirlineUpdateRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAirportUpdateRequest;
import com.insureth.insurance.model.dto.InsuranceAuditTrailModel;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceConfigService;
import com.insureth.insurance.service.flightinsurance.InsuranceAuditTrailService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insurance/admin/flightinsurance")
@RequiredArgsConstructor
public class FlightInsuranceAdminController {

    private final FlightInsuranceConfigService flightInsuranceConfigService;
    private final InsuranceAuditTrailService insuranceAuditTrailService;

    @GetMapping("/config")
    public ResponseEntity<FlightInsuranceAdminConfigResponse> getAdminConfig() {
        return ResponseEntity.ok(flightInsuranceConfigService.getAdminConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<FlightInsuranceAdminConfigResponse> updateAdminConfig(
            Authentication authentication,
            @Valid @RequestBody FlightInsuranceAdminConfigRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.updateAdminConfig(
                request,
                authentication != null ? authentication.getName() : null,
                "ADMIN",
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/airlines")
    public ResponseEntity<AdminPagedResponse<FlightInsuranceAdminAirlineModel>> getAirlines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirlinesPage(page, size, search));
    }

    @GetMapping("/airlines/{airlineId}")
    public ResponseEntity<FlightInsuranceAdminAirlineModel> getAirline(@PathVariable Long airlineId) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirline(airlineId));
    }

    @PutMapping("/airlines/{airlineId}")
    public ResponseEntity<FlightInsuranceAdminAirlineModel> updateAirline(
            @PathVariable Long airlineId,
            Authentication authentication,
            @Valid @RequestBody FlightInsuranceAirlineUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.updateAirline(
                airlineId,
                request,
                authentication != null ? authentication.getName() : null,
                "ADMIN",
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/airports")
    public ResponseEntity<AdminPagedResponse<FlightInsuranceAdminAirportModel>> getAirports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirportsPage(page, size, search));
    }

    @GetMapping("/audit-trail")
    public ResponseEntity<AdminPagedResponse<InsuranceAuditTrailModel>> getAuditTrails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(insuranceAuditTrailService.getAuditTrailPage(page, size, search));
    }

    @GetMapping("/airports/{airportId}")
    public ResponseEntity<FlightInsuranceAdminAirportModel> getAirport(@PathVariable Long airportId) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirport(airportId));
    }

    @PutMapping("/airports/{airportId}")
    public ResponseEntity<FlightInsuranceAdminAirportModel> updateAirport(
            @PathVariable Long airportId,
            Authentication authentication,
            @Valid @RequestBody FlightInsuranceAirportUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.updateAirport(
                airportId,
                request,
                authentication != null ? authentication.getName() : null,
                "ADMIN",
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }
}
