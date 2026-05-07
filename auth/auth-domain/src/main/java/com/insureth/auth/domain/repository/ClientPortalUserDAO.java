package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.ClientPortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientPortalUserDAO extends JpaRepository<ClientPortalUser, Long> {
    boolean existsByUserWalletAddressIgnoreCase(String walletAddress);
    java.util.Optional<ClientPortalUser> findByUserWalletAddressIgnoreCase(String walletAddress);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndUserUserIdNot(String username, Long userId);
    boolean existsByEmailIgnoreCaseAndUserUserIdNot(String email, Long userId);
}
