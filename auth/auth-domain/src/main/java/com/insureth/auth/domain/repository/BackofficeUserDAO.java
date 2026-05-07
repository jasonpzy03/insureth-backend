package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.BackofficeUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackofficeUserDAO extends JpaRepository<BackofficeUser, Long> {

    @Override
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.roleRights", "user"})
    List<BackofficeUser> findAll();

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.roleRights", "user"})
    Optional<BackofficeUser> findByUserWalletAddressIgnoreCase(String walletAddress);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserWalletAddressIgnoreCase(String walletAddress);
}
