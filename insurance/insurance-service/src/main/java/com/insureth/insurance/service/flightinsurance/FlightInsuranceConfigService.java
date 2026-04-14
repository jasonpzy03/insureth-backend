package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.Airline;
import com.insureth.insurance.domain.entity.Airport;
import com.insureth.insurance.domain.entity.FlightInsuranceProductConfig;
import com.insureth.insurance.domain.repository.AirlineDAO;
import com.insureth.insurance.domain.repository.AirportDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceAdminQuery;
import com.insureth.insurance.domain.repository.FlightInsuranceProductConfigDAO;
import com.insureth.insurance.model.dto.AdminPagedResponse;
import com.insureth.insurance.model.dto.AirlineResponseModel;
import com.insureth.insurance.model.dto.AirportResponseModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAdminConfigResponse;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirlineModel;
import com.insureth.insurance.model.dto.FlightInsuranceAdminAirportModel;
import com.insureth.insurance.model.dto.FlightInsuranceAirlineUpdateRequest;
import com.insureth.insurance.model.dto.FlightInsuranceAirportUpdateRequest;
import com.insureth.insurance.model.dto.FlightInsuranceExperienceConfigResponse;
import com.insureth.insurance.model.dto.FlightInsuranceProductConfigModel;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightInsuranceConfigService {

    private static final String PRODUCT_CODE = "flight-delay-v1";

    private final InsuranceAuditTrailService insuranceAuditTrailService;
    private final FlightInsuranceAdminQuery flightInsuranceAdminQueryDAO;
    private final FlightInsuranceProductConfigDAO productConfigDAO;
    private final AirlineDAO airlineDAO;
    private final AirportDAO airportDAO;

    @PostConstruct
    @Transactional
    public void initializeDefaults() {
        ensureDefaultProductConfig();
        ensureInitialAirlineFlags();
        ensureInitialAirportFlags();
    }

    @Transactional(readOnly = true)
    public FlightInsuranceExperienceConfigResponse getPublicConfig() {
        FlightInsuranceProductConfig product = getRequiredProductConfig();
        return FlightInsuranceExperienceConfigResponse.builder()
                .product(toProductModel(product))
                .supportedAirlines(getSupportedAirlines())
                .supportedAirports(getSupportedAirports())
                .build();
    }

    @Transactional(readOnly = true)
    public FlightInsuranceAdminConfigResponse getAdminConfig() {
        FlightInsuranceProductConfig product = getRequiredProductConfig();

        return FlightInsuranceAdminConfigResponse.builder()
                .product(toProductModel(product))
                .build();
    }

    @Transactional
    public FlightInsuranceAdminConfigResponse updateAdminConfig(
            FlightInsuranceAdminConfigRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        FlightInsuranceProductConfig product = getRequiredProductConfig();

        applyProductConfig(product, request.getProduct());
        productConfigDAO.save(product);
        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_PRICING_UPDATED",
                actorWalletAddress,
                actorRole,
                "FLIGHT_INSURANCE_PRODUCT",
                product.getProductCode(),
                "Updated flight insurance pricing and payout configuration",
                ipAddress,
                userAgent
        );

        return getAdminConfig();
    }

    public List<AirlineResponseModel> getSupportedAirlines() {
        List<Airline> supportedAirlines = airlineDAO.findByIataCodeIsNotNullAndSupportedForFlightInsuranceTrue();
        if (supportedAirlines.isEmpty()) {
            supportedAirlines = airlineDAO.findByIataCodeIsNotNull();
        }

        return supportedAirlines.stream()
                .sorted(Comparator.comparing(Airline::getName))
                .map(this::toAirlineModel)
                .toList();
    }

    public List<AirportResponseModel> getSupportedAirports() {
        List<Airport> supportedAirports = airportDAO.findByIataCodeIsNotNullAndSupportedForFlightInsuranceTrue();
        if (supportedAirports.isEmpty()) {
            supportedAirports = airportDAO.findByIataCodeIsNotNull();
        }

        return supportedAirports.stream()
                .sorted(Comparator.comparing(Airport::getName))
                .map(this::toAirportModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPagedResponse<FlightInsuranceAdminAirlineModel> getAirlinesPage(int page, int size, String search) {
        var airlinePage = flightInsuranceAdminQueryDAO.findAirlines(page, size, search);
        return AdminPagedResponse.<FlightInsuranceAdminAirlineModel>builder()
                .items(airlinePage.getContent().stream().map(this::toAdminAirlineModel).toList())
                .totalElements(airlinePage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminPagedResponse<FlightInsuranceAdminAirportModel> getAirportsPage(int page, int size, String search) {
        var airportPage = flightInsuranceAdminQueryDAO.findAirports(page, size, search);
        return AdminPagedResponse.<FlightInsuranceAdminAirportModel>builder()
                .items(airportPage.getContent().stream().map(this::toAdminAirportModel).toList())
                .totalElements(airportPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public FlightInsuranceAdminAirlineModel getAirline(Long airlineId) {
        Airline airline = airlineDAO.findById(airlineId).orElseThrow();
        return toAdminAirlineModel(airline);
    }

    @Transactional(readOnly = true)
    public FlightInsuranceAdminAirportModel getAirport(Long airportId) {
        Airport airport = airportDAO.findById(airportId).orElseThrow();
        return toAdminAirportModel(airport);
    }

    @Transactional
    public FlightInsuranceAdminAirlineModel updateAirline(
            Long airlineId,
            FlightInsuranceAirlineUpdateRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        Airline airline = airlineDAO.findById(airlineId).orElseThrow();
        airline.setName(request.getName().trim());
        airline.setIataCode(request.getIataCode().trim().toUpperCase(Locale.ROOT));
        airline.setIcaoCode(request.getIcaoCode().trim().toUpperCase(Locale.ROOT));
        airline.setSupportedForFlightInsurance(request.getSupportedForFlightInsurance());
        Airline saved = airlineDAO.save(airline);
        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_AIRLINE_UPDATED",
                actorWalletAddress,
                actorRole,
                "AIRLINE",
                String.valueOf(saved.getAirlineId()),
                "Updated airline " + saved.getName() + " support settings",
                ipAddress,
                userAgent
        );
        return toAdminAirlineModel(saved);
    }

    @Transactional
    public FlightInsuranceAdminAirportModel updateAirport(
            Long airportId,
            FlightInsuranceAirportUpdateRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        Airport airport = airportDAO.findById(airportId).orElseThrow();
        airport.setName(request.getName().trim());
        airport.setIataCode(request.getIataCode().trim().toUpperCase(Locale.ROOT));
        airport.setIcaoCode(request.getIcaoCode().trim().toUpperCase(Locale.ROOT));
        airport.setSupportedForFlightInsurance(request.getSupportedForFlightInsurance());
        Airport saved = airportDAO.save(airport);
        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_AIRPORT_UPDATED",
                actorWalletAddress,
                actorRole,
                "AIRPORT",
                String.valueOf(saved.getAirportId()),
                "Updated airport " + saved.getName() + " support settings",
                ipAddress,
                userAgent
        );
        return toAdminAirportModel(saved);
    }

    private void applyProductConfig(FlightInsuranceProductConfig entity, FlightInsuranceProductConfigModel model) {
        entity.setBasePremiumEth(model.getBasePremiumEth());
        entity.setDelayPayoutTier1Eth(model.getDelayPayoutTier1Eth());
        entity.setDelayPayoutTier2Eth(model.getDelayPayoutTier2Eth());
        entity.setDelayPayoutTier3Eth(model.getDelayPayoutTier3Eth());
        entity.setCancellationPayoutEth(model.getCancellationPayoutEth());
        entity.setDelayThresholdTier1Minutes(model.getDelayThresholdTier1Minutes());
        entity.setDelayThresholdTier2Minutes(model.getDelayThresholdTier2Minutes());
        entity.setDelayThresholdTier3Minutes(model.getDelayThresholdTier3Minutes());
        entity.setPremiumBaseRate(model.getPremiumBaseRate());
        entity.setPremiumPerDayMultiplier(model.getPremiumPerDayMultiplier());
        entity.setPremiumDemandMultiplier(model.getPremiumDemandMultiplier());
        entity.setPremiumMaxMultiplier(model.getPremiumMaxMultiplier());
        entity.setCurrency(model.getCurrency());
    }

    private FlightInsuranceProductConfig getRequiredProductConfig() {
        return productConfigDAO.findByProductCodeIgnoreCase(PRODUCT_CODE)
                .orElseThrow();
    }

    private void ensureDefaultProductConfig() {
        productConfigDAO.findByProductCodeIgnoreCase(PRODUCT_CODE)
                .orElseGet(() -> productConfigDAO.save(FlightInsuranceProductConfig.builder()
                        .productCode(PRODUCT_CODE)
                        .basePremiumEth(new BigDecimal("10.000000"))
                        .delayPayoutTier1Eth(new BigDecimal("15.000000"))
                        .delayPayoutTier2Eth(new BigDecimal("20.000000"))
                        .delayPayoutTier3Eth(new BigDecimal("30.000000"))
                        .cancellationPayoutEth(new BigDecimal("30.000000"))
                        .delayThresholdTier1Minutes(45)
                        .delayThresholdTier2Minutes(120)
                        .delayThresholdTier3Minutes(180)
                        .premiumBaseRate(new BigDecimal("1.000000"))
                        .premiumPerDayMultiplier(new BigDecimal("0.015000"))
                        .premiumDemandMultiplier(new BigDecimal("1.000000"))
                        .premiumMaxMultiplier(new BigDecimal("3.000000"))
                        .currency("ETH")
                        .build()));
    }

    private void ensureInitialAirlineFlags() {
        List<Airline> airlines = airlineDAO.findByIataCodeIsNotNull();
        boolean changed = false;
        for (Airline airline : airlines) {
            if (airline.getSupportedForFlightInsurance() == null) {
                airline.setSupportedForFlightInsurance(true);
                changed = true;
            }
        }
        if (changed) {
            airlineDAO.saveAll(airlines);
        }
    }

    private void ensureInitialAirportFlags() {
        List<Airport> airports = airportDAO.findByIataCodeIsNotNull();
        boolean changed = false;
        for (Airport airport : airports) {
            if (airport.getSupportedForFlightInsurance() == null) {
                airport.setSupportedForFlightInsurance(true);
                changed = true;
            }
        }
        if (changed) {
            airportDAO.saveAll(airports);
        }
    }

    private FlightInsuranceProductConfigModel toProductModel(FlightInsuranceProductConfig entity) {
        return FlightInsuranceProductConfigModel.builder()
                .productCode(entity.getProductCode())
                .basePremiumEth(entity.getBasePremiumEth())
                .delayPayoutTier1Eth(entity.getDelayPayoutTier1Eth())
                .delayPayoutTier2Eth(entity.getDelayPayoutTier2Eth())
                .delayPayoutTier3Eth(entity.getDelayPayoutTier3Eth())
                .cancellationPayoutEth(entity.getCancellationPayoutEth())
                .delayThresholdTier1Minutes(entity.getDelayThresholdTier1Minutes())
                .delayThresholdTier2Minutes(entity.getDelayThresholdTier2Minutes())
                .delayThresholdTier3Minutes(entity.getDelayThresholdTier3Minutes())
                .premiumBaseRate(entity.getPremiumBaseRate())
                .premiumPerDayMultiplier(entity.getPremiumPerDayMultiplier())
                .premiumDemandMultiplier(entity.getPremiumDemandMultiplier())
                .premiumMaxMultiplier(entity.getPremiumMaxMultiplier())
                .currency(entity.getCurrency())
                .build();
    }

    private AirlineResponseModel toAirlineModel(Airline airline) {
        AirlineResponseModel model = new AirlineResponseModel();
        model.setName(airline.getName());
        model.setIataCode(airline.getIataCode());
        return model;
    }

    private AirportResponseModel toAirportModel(Airport airport) {
        AirportResponseModel model = new AirportResponseModel();
        model.setName(airport.getName());
        model.setIataCode(airport.getIataCode());
        return model;
    }

    private FlightInsuranceAdminAirlineModel toAdminAirlineModel(Airline airline) {
        return FlightInsuranceAdminAirlineModel.builder()
                .airlineId(airline.getAirlineId())
                .name(airline.getName())
                .iataCode(airline.getIataCode())
                .icaoCode(airline.getIcaoCode())
                .supportedForFlightInsurance(Boolean.TRUE.equals(airline.getSupportedForFlightInsurance()))
                .build();
    }

    private FlightInsuranceAdminAirportModel toAdminAirportModel(Airport airport) {
        return FlightInsuranceAdminAirportModel.builder()
                .airportId(airport.getAirportId())
                .name(airport.getName())
                .iataCode(airport.getIataCode())
                .icaoCode(airport.getIcaoCode())
                .supportedForFlightInsurance(Boolean.TRUE.equals(airport.getSupportedForFlightInsurance()))
                .build();
    }
}
