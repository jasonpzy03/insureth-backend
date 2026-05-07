package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportDAO extends JpaRepository<Airport, Long> {
    List<Airport> findByIataCodeIsNotNull();
    List<Airport> findByIataCodeIsNotNullAndSupportedForFlightInsuranceTrue();
    Airport findFirstByIataCode(String iataCode);

}
