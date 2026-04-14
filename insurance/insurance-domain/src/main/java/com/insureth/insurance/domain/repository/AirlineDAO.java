package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirlineDAO extends JpaRepository<Airline, Long> {
    List<Airline> findByIataCodeIsNotNull();
    List<Airline> findByIataCodeIsNotNullAndSupportedForFlightInsuranceTrue();
}
