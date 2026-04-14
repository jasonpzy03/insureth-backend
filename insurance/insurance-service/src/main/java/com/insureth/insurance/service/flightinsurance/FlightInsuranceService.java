package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.Airline;
import com.insureth.insurance.domain.entity.Airport;
import com.insureth.insurance.domain.entity.FlightInsuranceFlightRating;
import com.insureth.insurance.domain.entity.FlightInsuranceProductConfig;
import com.insureth.insurance.domain.repository.AirlineDAO;
import com.insureth.insurance.domain.repository.AirportDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceFlightRatingDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceProductConfigDAO;
import com.insureth.insurance.model.dto.*;
import com.insureth.insurance.service.client.airlabs.AirLabsApiFeign;
import com.insureth.insurance.service.client.aviationstack.AviationStackApiFeign;
import com.insureth.insurance.service.client.flightstats.FlightStatsApiFeign;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightInsuranceService {
    private static final String PRODUCT_CODE = "flight-delay-v1";

    private final AirLabsApiFeign airLabsApiFeign;
    private final AviationStackApiFeign aviationStackApiFeign;
    private final FlightStatsApiFeign flightStatsApiFeign;
    private final AirlineDAO airlineDAO;
    private final AirportDAO airportDAO;
    private final FlightInsuranceProductConfigDAO flightInsuranceProductConfigDAO;
    private final FlightInsuranceFlightRatingDAO flightInsuranceFlightRatingDAO;

    public FlightScheduleResponseModel getFlightFutureSchedule(String airlineIATACode,
                                                               String flightNumber,
                                                               String departureAirportIATACode,
                                                               LocalDate departureDate) {
        FlightScheduleResponseModel flightScheduleResponseModel = new FlightScheduleResponseModel();
        List<AviationStackFlightScheduleDataModel> dataModelList = aviationStackApiFeign.getFlightFutureSchedule(
                airlineIATACode,
                flightNumber,
                departureDate,
                departureAirportIATACode,
                "departure"
                ).getData();
        log.info("dataModelList={}", dataModelList);

        AviationStackFlightScheduleDataModel dataModel = dataModelList.getFirst();
        AirportDetailModel arrivalAirportModel = dataModel.getArrival();
        AirportDetailModel departureAirportModel = dataModel.getDeparture();

        flightScheduleResponseModel.setFlightIATA(dataModel.getFlight().getIataNumber().toUpperCase());
        flightScheduleResponseModel.setArrivalTime(arrivalAirportModel.getScheduledTime());
        flightScheduleResponseModel.setDepartureTime(departureAirportModel.getScheduledTime());
        flightScheduleResponseModel.setArrivalAirportIATA(arrivalAirportModel.getIataCode().toUpperCase());
        flightScheduleResponseModel.setDepartureAirportIATA(departureAirportModel.getIataCode().toUpperCase());
        flightScheduleResponseModel.setDepartureAirportName(airportDAO.findFirstByIataCode(flightScheduleResponseModel.getDepartureAirportIATA()).getName());
        flightScheduleResponseModel.setArrivalAirportName(airportDAO.findFirstByIataCode(flightScheduleResponseModel.getArrivalAirportIATA()).getName());

        return flightScheduleResponseModel;
    }


    public List<AirlineResponseModel> getAllAirlinesWithIataCode() {
        return airlineDAO.findByIataCodeIsNotNull()
                .stream()
                .sorted(Comparator.comparing(Airline::getName))
                .map(airline -> {
                    AirlineResponseModel model = new AirlineResponseModel();
                    model.setName(airline.getName());
                    model.setIataCode(airline.getIataCode());
                    return model;
                })
                .collect(Collectors.toList());
    }

    public List<AirportResponseModel> getAllAirportsWithIataCode() {
        return airportDAO.findByIataCodeIsNotNull()
                .stream()
                .sorted(Comparator.comparing(Airport::getName))
                .map(airport -> {
                    AirportResponseModel model = new AirportResponseModel();
                    model.setName(airport.getName());
                    model.setIataCode(airport.getIataCode());
                    return model;
                })
                .collect(Collectors.toList());
    }

    public FlightInsuranceQuoteResponse getQuote(
            String airlineIATACode,
            String flightNumber,
            LocalDate departureDate
    ) {
        FlightInsuranceProductConfig productConfig = flightInsuranceProductConfigDAO
                .findByProductCodeIgnoreCase(PRODUCT_CODE)
                .orElseThrow(() -> new IllegalArgumentException("Flight insurance product config not found"));

        Optional<FlightInsuranceFlightRating> cachedRating = flightInsuranceFlightRatingDAO
                .findByAirlineCodeIgnoreCaseAndFlightNumberIgnoreCase(airlineIATACode, flightNumber);
        FlightInsuranceFlightRating rating = cachedRating
                .orElseGet(() -> fetchAndPersistFlightRating(airlineIATACode, flightNumber));

        int daysUntilDeparture = Math.max(0, (int) ChronoUnit.DAYS.between(LocalDate.now(), departureDate));
        BigDecimal daysMultiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(daysUntilDeparture).multiply(productConfig.getPremiumPerDayMultiplier())
        );

        BigDecimal observations = BigDecimal.valueOf(safeInt(rating.getObservations()));
        BigDecimal delayRate = BigDecimal.ONE.subtract(safeDecimal(rating.getOntimePercent()));
        BigDecimal severeDelayRate = ratio(rating.getLate45(), observations);
        BigDecimal disruptionRate = ratio(safeInt(rating.getCancelled()) + safeInt(rating.getDiverted()), observations);
        BigDecimal meanDelayFactor = safeDecimal(rating.getDelayMean())
                .divide(BigDecimal.valueOf(120), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        BigDecimal performanceMultiplier = BigDecimal.ONE
                .add(delayRate.multiply(productConfig.getPremiumDemandMultiplier()))
                .add(severeDelayRate.multiply(new BigDecimal("0.750000")))
                .add(disruptionRate.multiply(new BigDecimal("1.250000")))
                .add(meanDelayFactor.multiply(new BigDecimal("0.350000")));

        BigDecimal uncappedMultiplier = productConfig.getPremiumBaseRate()
                .multiply(daysMultiplier)
                .multiply(performanceMultiplier);
        BigDecimal totalMultiplier = uncappedMultiplier.min(productConfig.getPremiumMaxMultiplier())
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal quotedPremium = productConfig.getBasePremiumEth()
                .multiply(totalMultiplier)
                .setScale(6, RoundingMode.HALF_UP);

        return FlightInsuranceQuoteResponse.builder()
                .airlineIataCode(airlineIATACode.toUpperCase())
                .flightNumber(flightNumber)
                .quoteSource(cachedRating.isPresent() ? "DATABASE" : "EXTERNAL_API")
                .daysUntilDeparture(daysUntilDeparture)
                .basePremiumEth(productConfig.getBasePremiumEth())
                .daysMultiplier(daysMultiplier.setScale(6, RoundingMode.HALF_UP))
                .performanceMultiplier(performanceMultiplier.setScale(6, RoundingMode.HALF_UP))
                .totalMultiplier(totalMultiplier)
                .quotedPremiumEth(quotedPremium)
                .ontimePercent(safeDecimal(rating.getOntimePercent()).setScale(4, RoundingMode.HALF_UP))
                .delayMeanMinutes(safeDecimal(rating.getDelayMean()).setScale(2, RoundingMode.HALF_UP))
                .allStars(safeDecimal(rating.getAllStars()).setScale(2, RoundingMode.HALF_UP))
                .severeDelayRate(severeDelayRate.setScale(4, RoundingMode.HALF_UP))
                .disruptionRate(disruptionRate.setScale(4, RoundingMode.HALF_UP))
                .build();
    }

    private FlightInsuranceFlightRating fetchAndPersistFlightRating(String airlineIATACode, String flightNumber) {
        FlightStatsRatingsResponseModel response = flightStatsApiFeign.getFlightRatings(
                airlineIATACode.toUpperCase(),
                flightNumber
        );

        if (response.getRatings() == null || response.getRatings().isEmpty()) {
            throw new IllegalArgumentException("No performance rating found for the selected flight");
        }

        FlightStatsRatingsResponseModel.FlightStatsRatingModel ratingModel = response.getRatings().getFirst();
        FlightStatsRatingsResponseModel.FlightStatsAirlineModel airlineModel = response.getAppendix() != null
                && response.getAppendix().getAirlines() != null
                ? response.getAppendix().getAirlines().stream().findFirst().orElse(null)
                : null;

        FlightStatsRatingsResponseModel.FlightStatsAirportModel departureAirport = findAirport(
                response,
                ratingModel.getDepartureAirportFsCode()
        );
        FlightStatsRatingsResponseModel.FlightStatsAirportModel arrivalAirport = findAirport(
                response,
                ratingModel.getArrivalAirportFsCode()
        );

        FlightInsuranceFlightRating entity = flightInsuranceFlightRatingDAO
                .findByAirlineCodeIgnoreCaseAndFlightNumberIgnoreCase(airlineIATACode, flightNumber)
                .orElseGet(FlightInsuranceFlightRating::new);

        entity.setAirlineCode(airlineIATACode.toUpperCase());
        entity.setFlightNumber(flightNumber);
        entity.setAirlineName(airlineModel != null ? airlineModel.getName() : null);
        entity.setDepartureAirportCode(ratingModel.getDepartureAirportFsCode());
        entity.setDepartureAirportName(departureAirport != null ? departureAirport.getName() : null);
        entity.setArrivalAirportCode(ratingModel.getArrivalAirportFsCode());
        entity.setArrivalAirportName(arrivalAirport != null ? arrivalAirport.getName() : null);
        entity.setObservations(ratingModel.getObservations());
        entity.setOntime(ratingModel.getOntime());
        entity.setLate15(ratingModel.getLate15());
        entity.setLate30(ratingModel.getLate30());
        entity.setLate45(ratingModel.getLate45());
        entity.setCancelled(ratingModel.getCancelled());
        entity.setDiverted(ratingModel.getDiverted());
        entity.setOntimePercent(ratingModel.getOntimePercent());
        entity.setDelayObservations(ratingModel.getDelayObservations());
        entity.setDelayMean(ratingModel.getDelayMean());
        entity.setDelayStandardDeviation(ratingModel.getDelayStandardDeviation());
        entity.setDelayMin(ratingModel.getDelayMin());
        entity.setDelayMax(ratingModel.getDelayMax());
        entity.setAllOntimeCumulative(ratingModel.getAllOntimeCumulative());
        entity.setAllOntimeStars(ratingModel.getAllOntimeStars());
        entity.setAllDelayCumulative(ratingModel.getAllDelayCumulative());
        entity.setAllDelayStars(ratingModel.getAllDelayStars());
        entity.setAllStars(ratingModel.getAllStars());
        entity.setFetchedAt(Instant.now());

        return flightInsuranceFlightRatingDAO.save(entity);
    }

    private FlightStatsRatingsResponseModel.FlightStatsAirportModel findAirport(
            FlightStatsRatingsResponseModel response,
            String airportCode
    ) {
        if (response.getAppendix() == null || response.getAppendix().getAirports() == null) {
            return null;
        }

        return response.getAppendix().getAirports().stream()
                .filter(airport -> airportCode != null && airportCode.equalsIgnoreCase(airport.getFs()))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal ratio(Integer numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(safeInt(numerator))
                .divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
