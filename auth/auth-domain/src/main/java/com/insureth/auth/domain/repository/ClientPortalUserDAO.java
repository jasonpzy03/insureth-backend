package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.ClientPortalUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientPortalUserDAO extends JpaRepository<ClientPortalUser, Long> {
    boolean existsByWalletAddressIgnoreCase(String walletAddress);
}
