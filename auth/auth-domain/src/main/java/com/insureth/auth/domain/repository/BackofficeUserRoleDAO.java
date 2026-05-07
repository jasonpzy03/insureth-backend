package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.BackofficeUser;
import com.insureth.auth.domain.entity.BackofficeUserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackofficeUserRoleDAO extends JpaRepository<BackofficeUserRole, Long> {
    List<BackofficeUserRole> findByUser(BackofficeUser user);
    void deleteByUser(BackofficeUser user);
}
