package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.FlightInsuranceProductConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightInsuranceProductConfigDAO extends JpaRepository<FlightInsuranceProductConfig, Long> {
    Optional<FlightInsuranceProductConfig> findByProductCodeIgnoreCase(String productCode);
}
