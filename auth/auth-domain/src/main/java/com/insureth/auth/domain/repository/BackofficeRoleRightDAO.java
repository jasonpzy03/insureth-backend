package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.BackofficeRole;
import com.insureth.auth.domain.entity.BackofficeRoleRight;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackofficeRoleRightDAO extends JpaRepository<BackofficeRoleRight, Long> {
    List<BackofficeRoleRight> findByRole(BackofficeRole role);
}
