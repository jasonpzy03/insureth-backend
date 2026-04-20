package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.PendingClientPortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PendingClientPortalUserDAO extends JpaRepository<PendingClientPortalUser, String> {

    @Modifying
    @Query("DELETE FROM PendingClientPortalUser p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    void deleteByWalletAddressIgnoreCase(String walletAddress);
}
