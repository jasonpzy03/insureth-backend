package com.insureth.insurance.ws.controller.flightinsurance;

import com.insureth.insurance.model.dto.AdminPagedResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirlineModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirportModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicyRecordModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicySyncRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicySyncResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAirlineUpdateRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAirportUpdateRequest;
import com.insureth.insurance.model.dto.InsuranceAuditTrailModel;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceConfigService;
import com.insureth.insurance.service.flightinsurance.InsuranceAuditTrailService;
import com.insureth.insurance.service.flightinsurance.FlightInsurancePolicyService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insurance/admin/flightinsurance")
@RequiredArgsConstructor
public class FlightInsuranceAdminController {

    private final FlightInsuranceConfigService flightInsuranceConfigService;
    private final InsuranceAuditTrailService insuranceAuditTrailService;
    private final FlightInsurancePolicyService flightInsurancePolicyService;

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('INSURANCE_CONFIG_VIEW')")
    public ResponseEntity<FlightInsuranceAdminConfigResponse> getAdminConfig() {
        return ResponseEntity.ok(flightInsuranceConfigService.getAdminConfig());
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('INSURANCE_CONFIG_EDIT')")
    public ResponseEntity<FlightInsuranceAdminConfigResponse> updateAdminConfig(
            Authentication authentication,
            @Valid @RequestBody FlightInsuranceAdminConfigRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.updateAdminConfig(
                request,
                authentication != null ? authentication.getName() : null,
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/airlines")
    @PreAuthorize("hasAuthority('INSURANCE_AIRLINE_VIEW')")
    public ResponseEntity<AdminPagedResponse<FlightInsuranceAdminAirlineModel>> getAirlines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirlinesPage(page, size, search));
    }

    @GetMapping("/airlines/{airlineId}")
    @PreAuthorize("hasAuthority('INSURANCE_AIRLINE_VIEW')")
    public ResponseEntity<FlightInsuranceAdminAirlineModel> getAirline(@PathVariable Long airlineId) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirline(airlineId));
    }

    @PutMapping("/airlines/{airlineId}")
    @PreAuthorize("hasAuthority('INSURANCE_AIRLINE_EDIT')")
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
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/airports")
    @PreAuthorize("hasAuthority('INSURANCE_AIRPORT_VIEW')")
    public ResponseEntity<AdminPagedResponse<FlightInsuranceAdminAirportModel>> getAirports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirportsPage(page, size, search));
    }

    @GetMapping("/audit-trail")
    @PreAuthorize("hasAuthority('INSURANCE_AUDIT_VIEW')")
    public ResponseEntity<AdminPagedResponse<InsuranceAuditTrailModel>> getAuditTrails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(insuranceAuditTrailService.getAuditTrailPage(page, size, search));
    }

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('INSURANCE_CONFIG_VIEW')")
    public ResponseEntity<List<FlightInsuranceAdminPolicyRecordModel>> getPolicies() {
        return ResponseEntity.ok(flightInsurancePolicyService.getPolicies());
    }

    @PostMapping("/policies/sync")
    @PreAuthorize("hasAuthority('INSURANCE_CONFIG_EDIT')")
    public ResponseEntity<FlightInsuranceAdminPolicySyncResponse> syncPolicies(
            Authentication authentication,
            @Valid @RequestBody FlightInsuranceAdminPolicySyncRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(flightInsurancePolicyService.syncPolicies(
                request,
                authentication != null ? authentication.getName() : null,
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/airports/{airportId}")
    @PreAuthorize("hasAuthority('INSURANCE_AIRPORT_VIEW')")
    public ResponseEntity<FlightInsuranceAdminAirportModel> getAirport(@PathVariable Long airportId) {
        return ResponseEntity.ok(flightInsuranceConfigService.getAirport(airportId));
    }

    @PostMapping("/airports/sync")
    @PreAuthorize("hasAuthority('INSURANCE_AIRPORT_EDIT')")
    public ResponseEntity<Void> syncAirports(
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        flightInsuranceConfigService.syncAirports(
                authentication != null ? authentication.getName() : null,
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/airports/sync-timezones")
    @PreAuthorize("hasAuthority('INSURANCE_AIRPORT_EDIT')")
    public ResponseEntity<Void> syncTimezones(
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        flightInsuranceConfigService.syncTimezones(
                authentication != null ? authentication.getName() : null,
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/airlines/sync")
    @PreAuthorize("hasAuthority('INSURANCE_AIRLINE_EDIT')")
    public ResponseEntity<Void> syncAirlines(
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        flightInsuranceConfigService.syncAirlines(
                authentication != null ? authentication.getName() : null,
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/airports/{airportId}")
    @PreAuthorize("hasAuthority('INSURANCE_AIRPORT_EDIT')")
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
                resolvePrimaryRole(authentication),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    private String resolvePrimaryRole(Authentication authentication) {
        if (authentication == null) {
            return "BACKOFFICE_USER";
        }

        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("BACKOFFICE_USER");
    }
}
