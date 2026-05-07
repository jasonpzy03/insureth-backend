package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.AuthAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAuditTrailDAO extends JpaRepository<AuthAuditTrail, Long> {
}
