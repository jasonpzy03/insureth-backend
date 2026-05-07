package com.insureth.insurance.ws.controller.flightinsurance;

import com.insureth.insurance.model.dto.FlightInsuranceNotificationModel;
import com.insureth.insurance.model.dto.PolicyPurchaseConfirmationRequest;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceNotificationInboxService;
import com.insureth.insurance.service.flightinsurance.FlightInsuranceNotificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insurance/flightinsurance/notifications")
@RequiredArgsConstructor
public class FlightInsuranceNotificationController {

    private final FlightInsuranceNotificationService flightInsuranceNotificationService;
    private final FlightInsuranceNotificationInboxService flightInsuranceNotificationInboxService;

    @GetMapping
    public ResponseEntity<List<FlightInsuranceNotificationModel>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                flightInsuranceNotificationInboxService.getNotifications(authentication.getName(), limit)
        );
    }
}
