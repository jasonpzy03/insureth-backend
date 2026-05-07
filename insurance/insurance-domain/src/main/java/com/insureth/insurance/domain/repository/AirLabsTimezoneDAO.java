package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.AirLabsTimezone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirLabsTimezoneDAO extends JpaRepository<AirLabsTimezone, Long> {
    List<AirLabsTimezone> findByCountryCode(String countryCode);
    Optional<AirLabsTimezone> findByCountryCodeAndTimezone(String countryCode, String timezone);
}
