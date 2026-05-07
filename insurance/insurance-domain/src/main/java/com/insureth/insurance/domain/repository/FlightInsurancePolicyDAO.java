package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.FlightInsurancePolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightInsurancePolicyDAO extends JpaRepository<FlightInsurancePolicy, Long> {
    Optional<FlightInsurancePolicy> findByPolicyId(Long policyId);
    List<FlightInsurancePolicy> findAllByOrderByPolicyIdDesc();
}
