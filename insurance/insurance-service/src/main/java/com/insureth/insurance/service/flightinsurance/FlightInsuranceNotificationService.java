package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.model.dto.ClientUserModel;
import com.insureth.insurance.model.dto.PolicyPurchaseConfirmationRequest;
import com.insureth.insurance.model.dto.PolicyResolutionNotificationRequest;
import com.insureth.insurance.service.client.auth.AuthUserApiFeign;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightInsuranceNotificationService {

    private static final String EMAIL_QUEUE = "email-queue";
    private static final DateTimeFormatter EMAIL_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM uuuu, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private final AuthUserApiFeign authUserApiFeign;
    private final JmsTemplate jmsTemplate;

    @Value("${app.client-portal.url:http://localhost:4200}")
    private String clientPortalUrl;

    public void queuePolicyPurchaseConfirmation(
            String authenticatedWalletAddress,
            PolicyPurchaseConfirmationRequest request
    ) {
        ClientUserModel user = authUserApiFeign.getClientUserByWalletAddress(authenticatedWalletAddress);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", user.getEmail());
        emailRequest.put("subject", "Policy Confirmed: " + request.getFlightNumber() + " " + request.getDepartureAirportIata()
                + " to " + request.getArrivalAirportIata());
        emailRequest.put("template", "policy-purchase-confirmation.ftl");
        emailRequest.put("username", defaultValue(user.getUsername(), "Valued Customer"));
        emailRequest.put("policyId", defaultValue(request.getPolicyId(), "Pending confirmation"));
        emailRequest.put("flightNumber", request.getFlightNumber());
        emailRequest.put("departureAirportName", request.getDepartureAirportName());
        emailRequest.put("departureAirportIata", request.getDepartureAirportIata());
        emailRequest.put("arrivalAirportName", request.getArrivalAirportName());
        emailRequest.put("arrivalAirportIata", request.getArrivalAirportIata());
        emailRequest.put("departureTime", formatDepartureTime(request.getDepartureTime()));
        emailRequest.put("premiumPaidEth", request.getPremiumPaidEth());
        emailRequest.put("netPremiumEth", request.getNetPremiumEth());
        emailRequest.put("platformFeeEth", request.getPlatformFeeEth());
        emailRequest.put("currency", request.getCurrency());
        emailRequest.put("transactionHash", request.getTransactionHash());
        emailRequest.put("managePoliciesLink", clientPortalUrl + "/policies");

        log.info("Queueing policy purchase confirmation email for wallet={} email={}",
                authenticatedWalletAddress,
                user.getEmail());
        jmsTemplate.convertAndSend(EMAIL_QUEUE, emailRequest);
    }

    public void queuePolicyResolution(
            String walletAddress,
            PolicyResolutionNotificationRequest request
    ) {
        ClientUserModel user = authUserApiFeign.getClientUserByWalletAddress(walletAddress);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", user.getEmail());
        emailRequest.put("subject", "Policy Update: " + request.getFlightNumber() + " Resolved (" + request.getStatus() + ")");
        emailRequest.put("template", "policy-resolved.ftl");
        emailRequest.put("username", defaultValue(user.getUsername(), "Valued Customer"));
        emailRequest.put("policyId", defaultValue(request.getPolicyId(), "Details in dashboard"));
        emailRequest.put("flightNumber", request.getFlightNumber());
        emailRequest.put("origin", request.getOrigin());
        emailRequest.put("destination", request.getDestination());
        emailRequest.put("departureAirportName", defaultValue(request.getDepartureAirportName(), request.getOrigin()));
        emailRequest.put("arrivalAirportName", defaultValue(request.getArrivalAirportName(), request.getDestination()));
        emailRequest.put("status", request.getStatus());
        emailRequest.put("delayMinutes", request.getDelayMinutes());
        emailRequest.put("payoutEth", request.getPayoutEth());
        emailRequest.put("resolutionTime", request.getResolutionTime());
        emailRequest.put("transactionHash", request.getTransactionHash());
        emailRequest.put("hasPayout", String.valueOf(request.isHasPayout()));
        emailRequest.put("managePoliciesLink", clientPortalUrl + "/policies");

        log.info("Queueing policy resolution email for wallet={} email={} status={}",
                walletAddress,
                user.getEmail(),
                request.getStatus());
        jmsTemplate.convertAndSend(EMAIL_QUEUE, emailRequest);
    }

    public void queueInvestmentConfirmation(
            String walletAddress,
            String assetsEth,
            String sharesMinted,
            String transactionHash,
            Instant confirmationTime
    ) {
        ClientUserModel user = authUserApiFeign.getClientUserByWalletAddress(walletAddress);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", user.getEmail());
        emailRequest.put("subject", "Investment Confirmed: " + assetsEth + " ETH provided to Insureth Pool");
        emailRequest.put("template", "investment-confirmation.ftl");
        emailRequest.put("username", defaultValue(user.getUsername(), "Valued Investor"));
        emailRequest.put("assetsEth", assetsEth);
        emailRequest.put("sharesMinted", sharesMinted);
        emailRequest.put("transactionHash", transactionHash);
        emailRequest.put("confirmationTime", DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(confirmationTime));
        emailRequest.put("investorDashboardLink", clientPortalUrl + "/investor");

        log.info("Queueing investment confirmation email for wallet={} email={} amount={} ETH",
                walletAddress,
                user.getEmail(),
                assetsEth);
        jmsTemplate.convertAndSend(EMAIL_QUEUE, emailRequest);
    }

    public void queueWithdrawalConfirmation(
            String walletAddress,
            String assetsEth,
            String sharesBurned,
            String transactionHash,
            Instant confirmationTime
    ) {
        ClientUserModel user = authUserApiFeign.getClientUserByWalletAddress(walletAddress);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", user.getEmail());
        emailRequest.put("subject", "Withdrawal Confirmed: " + assetsEth + " ETH from Insureth Pool");
        emailRequest.put("template", "withdrawal-confirmation.ftl");
        emailRequest.put("username", defaultValue(user.getUsername(), "Valued Investor"));
        emailRequest.put("assetsEth", assetsEth);
        emailRequest.put("sharesBurned", sharesBurned);
        emailRequest.put("transactionHash", transactionHash);
        emailRequest.put("confirmationTime", DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(confirmationTime));
        emailRequest.put("investorDashboardLink", clientPortalUrl + "/investor");

        log.info("Queueing withdrawal confirmation email for wallet={} email={} amount={} ETH",
                walletAddress,
                user.getEmail(),
                assetsEth);
        jmsTemplate.convertAndSend(EMAIL_QUEUE, emailRequest);
    }

    private String formatDepartureTime(String departureTime) {
        try {
            return OffsetDateTime.parse(departureTime).format(EMAIL_DATETIME_FORMAT);
        } catch (Exception ignored) {
            return departureTime;
        }
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
