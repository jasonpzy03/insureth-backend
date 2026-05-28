package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.InsuranceGovernanceVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceGovernanceVoteDAO extends JpaRepository<InsuranceGovernanceVote, Long> {
    Optional<InsuranceGovernanceVote> findByEventKey(String eventKey);
    List<InsuranceGovernanceVote> findAllByProposalIdOrderByBlockNumberAscLogIndexAsc(Long proposalId);
}
