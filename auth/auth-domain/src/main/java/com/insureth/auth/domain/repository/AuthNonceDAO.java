package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.AuthNonce;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthNonceDAO extends JpaRepository<AuthNonce, Long> {
    Optional<AuthNonce> findFirstByUserUserIdAndNonceAndUsedAtIsNullOrderByCreatedAtDesc(Long userId, String nonce);
}
