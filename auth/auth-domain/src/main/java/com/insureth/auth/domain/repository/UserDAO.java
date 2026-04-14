package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {
    Optional<User> findByWalletAddressIgnoreCase(String walletAddress);
    boolean existsByWalletAddressIgnoreCase(String walletAddress);
}
