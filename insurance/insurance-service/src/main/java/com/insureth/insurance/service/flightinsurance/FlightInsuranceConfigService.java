package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.AirLabsTimezone;
import com.insureth.insurance.domain.entity.Airline;
import com.insureth.insurance.domain.entity.Airport;
import com.insureth.insurance.domain.entity.FlightInsuranceProductConfig;
import com.insureth.insurance.domain.repository.AirlineDAO;
import com.insureth.insurance.domain.repository.AirportDAO;
import com.insureth.insurance.domain.repository.AirLabsTimezoneDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceAdminQuery;
import com.insureth.insurance.domain.repository.FlightInsuranceProductConfigDAO;
import com.insureth.insurance.model.dto.AirLabsAirlineModel;
import com.insureth.insurance.model.dto.AirLabsAirlineResponse;
import com.insureth.insurance.model.dto.AirLabsAirportModel;
import com.insureth.insurance.model.dto.AirLabsAirportResponse;
import com.insureth.insurance.model.dto.AirLabsTimezoneResponse;
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
import com.insureth.insurance.service.client.airlabs.AirLabsApiFeign;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightInsuranceConfigService {

    private static final String PRODUCT_CODE = "flight-delay-v1";

    private final InsuranceAuditTrailService insuranceAuditTrailService;
    private final FlightInsuranceAdminQuery flightInsuranceAdminQueryDAO;
    private final FlightInsuranceProductConfigDAO productConfigDAO;
    private final AirLabsApiFeign airLabsApiFeign;
    private final AirlineDAO airlineDAO;
    private final AirportDAO airportDAO;
    private final AirLabsTimezoneDAO airLabsTimezoneDAO;

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
                "FLIGHT_INSURANCE_QUOTE_SETTINGS_UPDATED",
                actorWalletAddress,
                actorRole,
                "FLIGHT_INSURANCE_PRODUCT",
                product.getProductCode(),
                "Updated flight insurance quote engine configuration",
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
        airport.setTimezone(request.getTimezone() != null ? request.getTimezone().trim() : null);
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

    @Transactional
    public void syncAirports(
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        log.info("Starting airports sync from Airlabs API...");
        AirLabsAirportResponse apiResponse = airLabsApiFeign.getAirports("name,iata_code,icao_code,country_code");
        
        if (apiResponse == null || apiResponse.getResponse() == null) {
            log.warn("Airlabs API returned empty response");
            return;
        }

        List<AirLabsAirportModel> apiAirports = apiResponse.getResponse().stream()
                .filter(a -> a.getIataCode() != null && !a.getIataCode().isBlank())
                .filter(a -> a.getName() != null && !a.getName().trim().startsWith("("))
                .toList();

        log.info("Fetched {} valid airports from Airlabs", apiAirports.size());

        // Fetch all timezones from local cache
        Map<String, String> timezoneCache = airLabsTimezoneDAO.findAll().stream()
                .collect(Collectors.toMap(
                        AirLabsTimezone::getCountryCode,
                        AirLabsTimezone::getTimezone,
                        (existing, replacement) -> existing // Keep the first found (primary)
                ));

        // Fetch all existing airports to avoid N+1 queries during upsert
        Map<String, Airport> existingAirports = airportDAO.findAll().stream()
                .filter(a -> a.getIataCode() != null)
                .collect(Collectors.toMap(Airport::getIataCode, Function.identity(), (a, b) -> a));

        int updatedCount = 0;
        int createdCount = 0;

        for (AirLabsAirportModel apiAirport : apiAirports) {
            String countryTz = timezoneCache.get(apiAirport.getCountryCode());
            Airport airport = existingAirports.get(apiAirport.getIataCode());
            
            if (airport != null) {
                // Update existing
                airport.setName(apiAirport.getName());
                airport.setIcaoCode(apiAirport.getIcaoCode());
                airport.setCountryCode(apiAirport.getCountryCode());
                // Only update timezone if not already set manually
                if (airport.getTimezone() == null || airport.getTimezone().isBlank()) {
                    airport.setTimezone(countryTz);
                }
                updatedCount++;
            } else {
                // Create new
                airport = Airport.builder()
                        .name(apiAirport.getName())
                        .iataCode(apiAirport.getIataCode())
                        .icaoCode(apiAirport.getIcaoCode())
                        .countryCode(apiAirport.getCountryCode())
                        .timezone(countryTz)
                        .supportedForFlightInsurance(false)
                        .build();
                createdCount++;
            }
            airportDAO.save(airport);
        }

        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_AIRPORTS_SYNCED",
                actorWalletAddress,
                actorRole,
                "SYSTEM",
                "ALL",
                String.format("Synced airports from Airlabs: %d updated, %d created", updatedCount, createdCount),
                ipAddress,
                userAgent
        );

        log.info("Airports sync completed. Updated: {}, Created: {}", updatedCount, createdCount);
    }

    @Transactional
    public void syncAirlines(
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        log.info("Starting airlines sync from Airlabs API...");
        AirLabsAirlineResponse apiResponse = airLabsApiFeign.getAirlines("name,iata_code,icao_code");
        
        if (apiResponse == null || apiResponse.getResponse() == null) {
            log.warn("Airlabs API returned empty response");
            return;
        }

        List<AirLabsAirlineModel> apiAirlines = apiResponse.getResponse().stream()
                .filter(a -> a.getIataCode() != null && !a.getIataCode().isBlank())
                .filter(a -> !a.getIataCode().trim().endsWith("*"))
                .toList();

        log.info("Fetched {} valid airlines from Airlabs", apiAirlines.size());

        // Fetch all existing airlines to avoid N+1 queries during upsert
        Map<String, Airline> existingAirlines = airlineDAO.findAll().stream()
                .filter(a -> a.getIataCode() != null)
                .collect(Collectors.toMap(Airline::getIataCode, Function.identity(), (a, b) -> a));

        int updatedCount = 0;
        int createdCount = 0;

        for (AirLabsAirlineModel apiAirline : apiAirlines) {
            Airline airline = existingAirlines.get(apiAirline.getIataCode());
            
            if (airline != null) {
                // Update existing
                airline.setName(apiAirline.getName());
                airline.setIcaoCode(apiAirline.getIcaoCode());
                updatedCount++;
            } else {
                // Create new
                airline = Airline.builder()
                        .name(apiAirline.getName())
                        .iataCode(apiAirline.getIataCode())
                        .icaoCode(apiAirline.getIcaoCode())
                        .supportedForFlightInsurance(false)
                        .build();
                createdCount++;
            }
            airlineDAO.save(airline);
        }

        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_AIRLINES_SYNCED",
                actorWalletAddress,
                actorRole,
                "SYSTEM",
                "ALL",
                String.format("Synced airlines from Airlabs: %d updated, %d created", updatedCount, createdCount),
                ipAddress,
                userAgent
        );

        log.info("Airlines sync completed. Updated: {}, Created: {}", updatedCount, createdCount);
    }

    @Transactional
    public void syncTimezones(
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        log.info("Starting timezones sync from Airlabs API...");
        AirLabsTimezoneResponse apiResponse = airLabsApiFeign.getTimezones();

        if (apiResponse == null || apiResponse.getResponse() == null) {
            log.warn("Airlabs API returned empty response");
            return;
        }

        List<AirLabsTimezoneResponse.AirLabsTimezoneItem> apiTimezones = apiResponse.getResponse();
        log.info("Fetched {} timezones from Airlabs", apiTimezones.size());

        for (AirLabsTimezoneResponse.AirLabsTimezoneItem item : apiTimezones) {
            if (item.getCountryCode() == null || item.getTimezone() == null) continue;

            Optional<AirLabsTimezone> existing = airLabsTimezoneDAO.findByCountryCodeAndTimezone(
                    item.getCountryCode(), item.getTimezone());

            if (existing.isEmpty()) {
                AirLabsTimezone entity = AirLabsTimezone.builder()
                        .countryCode(item.getCountryCode())
                        .timezone(item.getTimezone())
                        .gmtOffset(item.getGmt())
                        .dstOffset(item.getDst())
                        .build();
                airLabsTimezoneDAO.save(entity);
            }
        }

        insuranceAuditTrailService.record(
                "FLIGHT_INSURANCE_TIMEZONES_SYNCED",
                actorWalletAddress,
                actorRole,
                "SYSTEM",
                "ALL",
                String.format("Synced %d timezones from Airlabs", apiTimezones.size()),
                ipAddress,
                userAgent
        );

        log.info("Timezones sync completed.");
    }

    private void applyProductConfig(FlightInsuranceProductConfig entity, FlightInsuranceProductConfigModel model) {
        entity.setBasePremiumEth(model.getBasePremiumEth());
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

    private FlightInsuranceProductConfigModel toProductModel(FlightInsuranceProductConfig entity) {
        return FlightInsuranceProductConfigModel.builder()
                .productCode(entity.getProductCode())
                .basePremiumEth(entity.getBasePremiumEth())
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
        model.setTimezone(airport.getTimezone());
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
                .timezone(airport.getTimezone())
                .countryCode(airport.getCountryCode())
                .supportedForFlightInsurance(Boolean.TRUE.equals(airport.getSupportedForFlightInsurance()))
                .build();
    }
}
