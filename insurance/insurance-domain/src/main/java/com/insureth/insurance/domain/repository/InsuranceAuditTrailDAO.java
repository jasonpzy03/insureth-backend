package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.InsuranceAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceAuditTrailDAO extends JpaRepository<InsuranceAuditTrail, Long> {
}
