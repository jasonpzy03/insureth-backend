package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.Airline;
import com.insureth.insurance.domain.entity.Airport;
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
public class FlightInsuranceAdminQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Airline> findAirlines(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Airline> query = builder.createQuery(Airline.class);
        Root<Airline> root = query.from(Airline.class);
        Predicate[] predicates = buildAirlinePredicates(builder, root, search);

        query.select(root)
                .where(predicates)
                .orderBy(builder.asc(root.get("name")));

        TypedQuery<Airline> typedQuery = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Airline> countRoot = countQuery.from(Airline.class);
        countQuery.select(builder.count(countRoot))
                .where(buildAirlinePredicates(builder, countRoot, search));

        long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    public Page<Airport> findAirports(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Airport> query = builder.createQuery(Airport.class);
        Root<Airport> root = query.from(Airport.class);
        Predicate[] predicates = buildAirportPredicates(builder, root, search);

        query.select(root)
                .where(predicates)
                .orderBy(builder.asc(root.get("name")));

        TypedQuery<Airport> typedQuery = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Airport> countRoot = countQuery.from(Airport.class);
        countQuery.select(builder.count(countRoot))
                .where(buildAirportPredicates(builder, countRoot, search));

        long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private Predicate[] buildAirlinePredicates(CriteriaBuilder builder, Root<Airline> root, String search) {
        List<Predicate> predicates = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(root.get("name")), pattern),
                            builder.like(builder.lower(root.get("iataCode")), pattern),
                            builder.like(builder.lower(root.get("icaoCode")), pattern)
                    )
            );
        }
        return predicates.toArray(Predicate[]::new);
    }

    private Predicate[] buildAirportPredicates(CriteriaBuilder builder, Root<Airport> root, String search) {
        List<Predicate> predicates = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(root.get("name")), pattern),
                            builder.like(builder.lower(root.get("iataCode")), pattern),
                            builder.like(builder.lower(root.get("icaoCode")), pattern)
                    )
            );
        }
        return predicates.toArray(Predicate[]::new);
    }
}
