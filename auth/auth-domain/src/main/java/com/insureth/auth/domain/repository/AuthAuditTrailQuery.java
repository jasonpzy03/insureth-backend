package com.insureth.auth.domain.repository;

import com.insureth.auth.domain.entity.AuthAuditTrail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class AuthAuditTrailQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<AuthAuditTrail> findAuditTrails(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<AuthAuditTrail> query = builder.createQuery(AuthAuditTrail.class);
        Root<AuthAuditTrail> root = query.from(AuthAuditTrail.class);
        Predicate[] predicates = buildPredicates(builder, root, search);

        query.select(root)
                .where(predicates)
                .orderBy(builder.desc(root.get("createdAt")));

        TypedQuery<AuthAuditTrail> typedQuery = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<AuthAuditTrail> countRoot = countQuery.from(AuthAuditTrail.class);
        countQuery.select(builder.count(countRoot))
                .where(buildPredicates(builder, countRoot, search));

        long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private Predicate[] buildPredicates(CriteriaBuilder builder, Root<AuthAuditTrail> root, String search) {
        List<Predicate> predicates = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(root.get("eventType")), pattern),
                            builder.like(builder.lower(root.get("actorWalletAddress")), pattern),
                            builder.like(builder.lower(root.get("actorRole")), pattern),
                            builder.like(builder.lower(root.get("targetType")), pattern),
                            builder.like(builder.lower(root.get("targetIdentifier")), pattern),
                            builder.like(builder.lower(root.get("description")), pattern),
                            builder.like(builder.lower(root.get("ipAddress")), pattern)
                    )
            );
        }
        return predicates.toArray(Predicate[]::new);
    }
}
