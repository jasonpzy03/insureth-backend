package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.BackofficeUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackofficeUserDAO extends JpaRepository<BackofficeUser, Long> {
    Optional<BackofficeUser> findByUserWalletAddressIgnoreCase(String walletAddress);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserWalletAddressIgnoreCase(String walletAddress);
}
