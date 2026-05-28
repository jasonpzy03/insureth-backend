package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.InsuranceGovernanceProposal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceGovernanceProposalDAO extends JpaRepository<InsuranceGovernanceProposal, Long> {
    List<InsuranceGovernanceProposal> findAllByOrderByProposalIdDesc();
}
