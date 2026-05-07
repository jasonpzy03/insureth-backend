package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.BackofficeRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackofficeRoleDAO extends JpaRepository<BackofficeRole, Long> {

    @Override
    @EntityGraph(attributePaths = {"roleRights"})
    List<BackofficeRole> findAll();

    @EntityGraph(attributePaths = {"roleRights"})
    Optional<BackofficeRole> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
