package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.FlightInsuranceFlightRating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightInsuranceFlightRatingDAO extends JpaRepository<FlightInsuranceFlightRating, Long> {
    Optional<FlightInsuranceFlightRating> findByAirlineCodeIgnoreCaseAndFlightNumberIgnoreCase(
            String airlineCode,
            String flightNumber
    );
}
