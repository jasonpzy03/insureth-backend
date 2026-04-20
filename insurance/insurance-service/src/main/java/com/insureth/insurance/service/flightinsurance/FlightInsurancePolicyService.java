package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.FlightInsurancePolicy;
import com.insureth.insurance.domain.repository.FlightInsurancePolicyDAO;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicyRecordModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicySyncRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAdminPolicySyncResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightInsurancePolicyService {

    private final FlightInsurancePolicyDAO flightInsurancePolicyDAO;
    private final InsuranceAuditTrailService insuranceAuditTrailService;

    @Transactional(readOnly = true)
    public List<FlightInsuranceAdminPolicyRecordModel> getPolicies() {
        return flightInsurancePolicyDAO.findAllByOrderByPolicyIdDesc().stream()
                .map(this::toModel)
                .toList();
    }

    @Transactional
    public FlightInsuranceAdminPolicySyncResponse syncPolicies(
            FlightInsuranceAdminPolicySyncRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        Instant syncedAt = Instant.now();

        for (FlightInsuranceAdminPolicyRecordModel policy : request.getPolicies()) {
            FlightInsurancePolicy storedPolicy = flightInsurancePolicyDAO.findByPolicyId(policy.getPolicyId())
                    .orElseGet(FlightInsurancePolicy::new);

            storedPolicy.setPolicyId(policy.getPolicyId());
            storedPolicy.setHolder(policy.getHolder());
            storedPolicy.setRiskKey(policy.getRiskKey());
            storedPolicy.setFlightNumber(policy.getFlightNumber());
            storedPolicy.setOrigin(policy.getOrigin());
            storedPolicy.setDestination(policy.getDestination());
            storedPolicy.setDepartureTime(policy.getDepartureTime());
            storedPolicy.setPurchaseTimestamp(policy.getPurchaseTimestamp());
            storedPolicy.setPremiumWei(policy.getPremiumWei());
            storedPolicy.setPayoutAmountWei(policy.getPayoutAmountWei());
            storedPolicy.setActive(Boolean.TRUE.equals(policy.getActive()));
            storedPolicy.setClaimed(Boolean.TRUE.equals(policy.getClaimed()));
            storedPolicy.setResolved(Boolean.TRUE.equals(policy.getResolved()));
            storedPolicy.setExistsOnChain(Boolean.TRUE.equals(policy.getExists()));
            storedPolicy.setOracleRequested(Boolean.TRUE.equals(policy.getOracleRequested()));
            storedPolicy.setStatusInt(policy.getStatusInt());
            storedPolicy.setStatusLabel(policy.getStatusLabel());
            storedPolicy.setDelayMinutes(policy.getDelayMinutes());
            storedPolicy.setPoliciesSharingRisk(policy.getPoliciesSharingRisk());
            storedPolicy.setLastSyncedAt(syncedAt);

            flightInsurancePolicyDAO.save(storedPolicy);
        }

        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_POLICIES_SYNCED",
                actorWalletAddress,
                actorRole,
                "FLIGHT_INSURANCE_POLICY",
                "ALL",
                "Synced " + request.getPolicies().size() + " flight insurance policies from chain into the insurance database",
                ipAddress,
                userAgent
        );

        return FlightInsuranceAdminPolicySyncResponse.builder()
                .syncedPolicies(request.getPolicies().size())
                .syncedAt(syncedAt)
                .build();
    }

    private FlightInsuranceAdminPolicyRecordModel toModel(FlightInsurancePolicy policy) {
        return FlightInsuranceAdminPolicyRecordModel.builder()
                .policyId(policy.getPolicyId())
                .holder(policy.getHolder())
                .riskKey(policy.getRiskKey())
                .flightNumber(policy.getFlightNumber())
                .origin(policy.getOrigin())
                .destination(policy.getDestination())
                .departureTime(policy.getDepartureTime())
                .purchaseTimestamp(policy.getPurchaseTimestamp())
                .premiumWei(policy.getPremiumWei())
                .payoutAmountWei(policy.getPayoutAmountWei())
                .active(Boolean.TRUE.equals(policy.getActive()))
                .claimed(Boolean.TRUE.equals(policy.getClaimed()))
                .resolved(Boolean.TRUE.equals(policy.getResolved()))
                .exists(Boolean.TRUE.equals(policy.getExistsOnChain()))
                .oracleRequested(Boolean.TRUE.equals(policy.getOracleRequested()))
                .statusInt(policy.getStatusInt())
                .statusLabel(policy.getStatusLabel())
                .delayMinutes(policy.getDelayMinutes())
                .policiesSharingRisk(policy.getPoliciesSharingRisk())
                .build();
    }
}
