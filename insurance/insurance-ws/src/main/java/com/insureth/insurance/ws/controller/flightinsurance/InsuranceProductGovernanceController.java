package com.insureth.insurance.ws.controller.flightinsurance;

import com.insureth.insurance.model.dto.InsuranceGovernanceProposalDetailModel;
import com.insureth.insurance.model.dto.InsuranceGovernanceProposalModel;
import com.insureth.insurance.model.dto.InsuranceGovernanceTransactionRecordRequest;
import com.insureth.insurance.service.flightinsurance.InsuranceProductGovernanceService;
import com.insureth.insurance.service.flightinsurance.InsuranceProductGovernanceTransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InsuranceProductGovernanceController {

    private final InsuranceProductGovernanceService governanceService;
    private final InsuranceProductGovernanceTransactionService governanceTransactionService;

    @GetMapping("/api/v1/insurance/governance/product-roadmap/proposals")
    public ResponseEntity<List<InsuranceGovernanceProposalModel>> listProposals() {
        return ResponseEntity.ok(governanceService.listProposals());
    }

    @GetMapping("/api/v1/insurance/governance/product-roadmap/proposals/{proposalId}")
    public ResponseEntity<InsuranceGovernanceProposalDetailModel> getProposal(@PathVariable Long proposalId) {
        return ResponseEntity.ok(governanceService.getProposal(proposalId));
    }

    @PostMapping("/api/v1/insurance/admin/governance/product-roadmap/proposals/record-create")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_GOVERNANCE_MANAGER')")
    public ResponseEntity<Void> recordProposalCreate(
            @RequestBody InsuranceGovernanceTransactionRecordRequest request
    ) {
        governanceTransactionService.recordProposalCreated(request.getTransactionHash());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/insurance/admin/governance/product-roadmap/proposals/record-cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_GOVERNANCE_MANAGER')")
    public ResponseEntity<Void> recordProposalCancel(
            @RequestBody InsuranceGovernanceTransactionRecordRequest request
    ) {
        governanceTransactionService.recordProposalCancelled(request.getTransactionHash());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/insurance/admin/governance/product-roadmap/proposals/record-execute")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_GOVERNANCE_MANAGER')")
    public ResponseEntity<Void> recordProposalExecute(
            @RequestBody InsuranceGovernanceTransactionRecordRequest request
    ) {
        governanceTransactionService.recordProposalExecuted(request.getTransactionHash());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/insurance/governance/product-roadmap/votes/record")
    public ResponseEntity<Void> recordVote(
            @RequestBody InsuranceGovernanceTransactionRecordRequest request
    ) {
        governanceTransactionService.recordVoteCast(request.getTransactionHash());
        return ResponseEntity.ok().build();
    }
}
