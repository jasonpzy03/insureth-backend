package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.Airport;
import com.insureth.insurance.domain.entity.FlightInsuranceNotificationRecord;
import com.insureth.insurance.domain.entity.FlightInsuranceNotificationSyncState;
import com.insureth.insurance.domain.repository.AirportDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceNotificationRecordDAO;
import com.insureth.insurance.domain.repository.FlightInsuranceNotificationSyncStateDAO;
import com.insureth.insurance.model.dto.PolicyPurchaseConfirmationRequest;
import com.insureth.insurance.model.dto.PolicyResolutionNotificationRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.utils.Numeric;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightInsuranceNotificationSyncService {

    private static final String SYNC_KEY = "CLIENT_PORTAL_NOTIFICATIONS";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private enum FlightStatus {
        Unknown, OnTime, Delayed, Cancelled
    }

    private static final String TYPE_POLICY_PURCHASED = "POLICY_PURCHASED";
    private static final String TYPE_PAYOUT_DISTRIBUTED = "PAYOUT_DISTRIBUTED";
    private static final String TYPE_FLIGHT_RESOLVED = "FLIGHT_RESOLVED";
    private static final String TYPE_LIQUIDITY_PROVIDED = "LIQUIDITY_PROVIDED";
    private static final String TYPE_LIQUIDITY_WITHDRAWN = "LIQUIDITY_WITHDRAWN";

    private static final Event POLICY_PURCHASED_EVENT = new Event(
            "PolicyPurchased",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Bytes32>(true) {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event AUTO_PAYOUT_TRIGGERED_EVENT = new Event(
            "AutoPayoutTriggered",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event FLIGHT_STATUS_RESOLVED_EVENT = new Event(
            "FlightStatusResolved",
            List.of(
                    new TypeReference<Bytes32>(true) {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event LIQUIDITY_PROVIDED_EVENT = new Event(
            "LiquidityProvided",
            List.of(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event LIQUIDITY_WITHDRAWN_EVENT = new Event(
            "LiquidityWithdrawn",
            List.of(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private final Web3j web3j;
    private final FlightInsuranceNotificationRecordDAO flightInsuranceNotificationRecordDAO;
    private final FlightInsuranceNotificationSyncStateDAO flightInsuranceNotificationSyncStateDAO;
    private final FlightInsuranceNotificationService flightInsuranceNotificationService;
    private final FlightInsurancePolicyNftMetadataService flightInsurancePolicyNftMetadataService;
    private final AirportDAO airportDAO;

    @Value("${flight-insurance.contract-address:}")
    private String contractAddress;

    @Value("${flight-insurance.platform-fee-percentage:5}")
    private int platformFeePercentage;

    @Value("${flight-insurance.notifications.sync-from-block:0}")
    private long syncFromBlock;

    @Value("${flight-insurance.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Scheduled(
            initialDelayString = "${flight-insurance.notifications.initial-delay-ms:15000}",
            fixedDelayString = "${flight-insurance.notifications.sync-interval-ms:60000}"
    )
    @Transactional
    public void syncNotifications() {
        if (!notificationsEnabled) {
            return;
        }

        if (contractAddress == null || contractAddress.isBlank()) {
            log.debug("Skipping flight insurance notification sync because contract address is not configured.");
            return;
        }

        try {
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
            long fromBlock = resolveFromBlock();
            if (latestBlock.longValue() < fromBlock) {
                return;
            }

            EthFilter filter = new EthFilter(
                    DefaultBlockParameter.valueOf(BigInteger.valueOf(fromBlock)),
                    DefaultBlockParameter.valueOf(latestBlock),
                    normalizeContractAddress(contractAddress)
            );

            EthLog response = web3j.ethGetLogs(filter).send();
            if (response.hasError()) {
                log.warn(
                        "Skipping flight insurance notification sync because eth_getLogs returned an error: code={}, message={}",
                        response.getError().getCode(),
                        response.getError().getMessage()
                );
                return;
            }

            if (response.getLogs() == null) {
                log.warn("Skipping flight insurance notification sync because eth_getLogs returned no logs payload.");
                return;
            }

            Set<String> trackedTopics = trackedTopics();
            List<Log> logs = response.getLogs().stream()
                    .map(logResult -> (Log) logResult.get())
                    .filter(logEntry -> logEntry.getTopics() != null && !logEntry.getTopics().isEmpty())
                    .filter(logEntry -> trackedTopics.contains(hexTopic(logEntry.getTopics().get(0))))
                    .collect(Collectors.toList());

            if (logs.isEmpty()) {
                saveSyncState(latestBlock.longValue());
                return;
            }

            Map<String, Instant> blockTimestampCache = fetchBlockTimestampsBatch(logs);
            processLogs(logs, blockTimestampCache);
            saveSyncState(latestBlock.longValue());
        } catch (ClientConnectionException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("429")) {
                log.warn("Rate limit (429) hit during blockchain sync. Will retry in the next cycle. Consider increasing sync-interval-ms.");
            } else {
                log.error("Network error during blockchain sync", exception);
            }
        } catch (Exception exception) {
            log.error("Failed to sync flight insurance notifications from chain", exception);
        }
    }

    private Map<String, Instant> fetchBlockTimestampsBatch(List<Log> logs) throws IOException {
        Set<String> blockHashes = logs.stream()
                .map(Log::getBlockHash)
                .collect(Collectors.toSet());

        BatchRequest batch = web3j.newBatch();
        Map<String, org.web3j.protocol.core.Request<?, EthBlock>> requestMap = new HashMap<>();

        for (String hash : blockHashes) {
            org.web3j.protocol.core.Request<?, EthBlock> request = web3j.ethGetBlockByHash(hash, false);
            requestMap.put(hash, request);
            batch.add(request);
        }

        BatchResponse batchResponse = batch.send();
        Map<String, Instant> timestampCache = new HashMap<>();

        for (Map.Entry<String, org.web3j.protocol.core.Request<?, EthBlock>> entry : requestMap.entrySet()) {
            EthBlock blockResponse = batchResponse.getResponses().stream()
                    .filter(response -> response.getId() == entry.getValue().getId())
                    .map(response -> (EthBlock) response)
                    .findFirst()
                    .orElse(null);

            if (blockResponse != null && blockResponse.getBlock() != null) {
                Instant timestamp = Instant.ofEpochSecond(blockResponse.getBlock().getTimestamp().longValueExact());
                timestampCache.put(entry.getKey(), timestamp);
            }
        }

        return timestampCache;
    }

    private void processLogs(List<Log> logs, Map<String, Instant> blockTimestampCache) throws IOException {
        String policyPurchasedTopic = hexTopic(EventEncoder.encode(POLICY_PURCHASED_EVENT));
        String autoPayoutTopic = hexTopic(EventEncoder.encode(AUTO_PAYOUT_TRIGGERED_EVENT));
        String flightResolvedTopic = hexTopic(EventEncoder.encode(FLIGHT_STATUS_RESOLVED_EVENT));
        String liquidityTopic = hexTopic(EventEncoder.encode(LIQUIDITY_PROVIDED_EVENT));
        String withdrawalTopic = hexTopic(EventEncoder.encode(LIQUIDITY_WITHDRAWN_EVENT));

        for (Log logEntry : logs) {
            String topic0 = hexTopic(logEntry.getTopics().get(0));

            if (topic0.equals(policyPurchasedTopic)) {
                handlePolicyPurchasedLog(logEntry, blockTimestampCache);
            } else if (topic0.equals(autoPayoutTopic)) {
                handleAutoPayoutLog(logEntry, blockTimestampCache);
            } else if (topic0.equals(flightResolvedTopic)) {
                handleFlightStatusResolvedLog(logEntry, blockTimestampCache);
            } else if (topic0.equals(liquidityTopic)) {
                handleLiquidityProvidedLog(logEntry, blockTimestampCache);
            } else if (topic0.equals(withdrawalTopic)) {
                handleLiquidityWithdrawnLog(logEntry, blockTimestampCache);
            }
        }
    }

    private void handlePolicyPurchasedLog(Log logEntry, Map<String, Instant> blockTimestampCache) throws IOException {
        String eventKey = buildEventKey(TYPE_POLICY_PURCHASED, logEntry);
        if (flightInsuranceNotificationRecordDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        if (logEntry.getTopics().size() < 4) {
            return;
        }

        long policyId = decodeUint256Topic(logEntry.getTopics().get(1)).longValueExact();
        String holder = decodeAddressTopic(logEntry.getTopics().get(2));
        String riskKey = logEntry.getTopics().get(3);
        List<Type> values = FunctionReturnDecoder.decode(
                logEntry.getData(),
                POLICY_PURCHASED_EVENT.getNonIndexedParameters()
        );
        String flightNumber = ((Utf8String) values.get(0)).getValue();
        String origin = ((Utf8String) values.get(1)).getValue();
        String destination = ((Utf8String) values.get(2)).getValue();
        long departureTime = ((Uint256) values.get(3)).getValue().longValueExact();
        String premiumWei = ((Uint256) values.get(4)).getValue().toString();
        Instant eventTimestamp = blockTimestampCache.getOrDefault(logEntry.getBlockHash(), Instant.now());

        flightInsuranceNotificationRecordDAO.save(FlightInsuranceNotificationRecord.builder()
                .eventKey(eventKey)
                .holder(holder)
                .policyId(policyId)
                .riskKey(riskKey)
                .type(TYPE_POLICY_PURCHASED)
                .title("Policy Purchased")
                .message(flightNumber + " " + origin + " -> " + destination + " coverage was purchased successfully.")
                .amountWei(premiumWei)
                .flightNumber(flightNumber)
                .origin(origin)
                .destination(destination)
                .departureTime(departureTime)
                .transactionHash(logEntry.getTransactionHash())
                .blockNumber(logEntry.getBlockNumber().longValueExact())
                .logIndex(logEntry.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .build());

        log.info("About to sync NFT metadata for policy={}", policyId);

        try {
            log.info("Entered syncPolicyMetadata for policy={}", policyId);
            flightInsurancePolicyNftMetadataService.syncPolicyMetadata(
                    policyId,
                    holder,
                    riskKey,
                    flightNumber,
                    origin,
                    destination,
                    departureTime,
                    premiumWei
            );
        } catch (Exception exception) {
            log.error("Failed to sync NFT metadata for policy={}", policyId, exception);
        }

        try {
            String departureAirportName = resolveAirportName(origin);
            String arrivalAirportName = resolveAirportName(destination);
            BigDecimal grossWei = new BigDecimal(premiumWei);
            BigDecimal feeWei = grossWei.multiply(BigDecimal.valueOf(platformFeePercentage))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            BigDecimal netWei = grossWei.subtract(feeWei);

            flightInsuranceNotificationService.queuePolicyPurchaseConfirmation(
                    holder,
                    PolicyPurchaseConfirmationRequest.builder()
                            .policyId(String.valueOf(policyId))
                            .flightNumber(flightNumber)
                            .departureAirportIata(origin)
                            .arrivalAirportIata(destination)
                            .departureAirportName(departureAirportName)
                            .arrivalAirportName(arrivalAirportName)
                            .departureTime(Instant.ofEpochSecond(departureTime).toString())
                            .premiumPaidEth(formatWeiToEth(grossWei.toBigInteger().toString()))
                            .netPremiumEth(formatWeiToEth(netWei.toBigInteger().toString()))
                            .platformFeeEth(formatWeiToEth(feeWei.toBigInteger().toString()))
                            .currency("ETH")
                            .transactionHash(logEntry.getTransactionHash())
                            .build()
            );
        } catch (Exception exception) {
            log.error("Failed to queue purchase email for policy={}", policyId, exception);
        }
    }

    private void handleAutoPayoutLog(Log logEntry, Map<String, Instant> blockTimestampCache) throws IOException {
        String eventKey = buildEventKey(TYPE_PAYOUT_DISTRIBUTED, logEntry);
        if (flightInsuranceNotificationRecordDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        if (logEntry.getTopics().size() < 3) {
            return;
        }

        long policyId = decodeUint256Topic(logEntry.getTopics().get(1)).longValueExact();
        String holder = decodeAddressTopic(logEntry.getTopics().get(2));
        String payoutWei = ((Uint256) FunctionReturnDecoder.decode(
                logEntry.getData(),
                AUTO_PAYOUT_TRIGGERED_EVENT.getNonIndexedParameters()
        ).get(0)).getValue().toString();
        Instant eventTimestamp = blockTimestampCache.getOrDefault(logEntry.getBlockHash(), Instant.now());

        Optional<FlightInsuranceNotificationRecord> purchaseNotification = flightInsuranceNotificationRecordDAO
                .findFirstByPolicyIdAndTypeOrderByEventTimestampDescCreatedAtDesc(policyId, TYPE_POLICY_PURCHASED);

        String flightNumber = purchaseNotification.map(FlightInsuranceNotificationRecord::getFlightNumber).orElse("Policy");
        String origin = purchaseNotification.map(FlightInsuranceNotificationRecord::getOrigin).orElse(null);
        String destination = purchaseNotification.map(FlightInsuranceNotificationRecord::getDestination).orElse(null);
        String message = buildPayoutMessage(policyId, flightNumber, origin, destination);

        flightInsuranceNotificationRecordDAO.save(FlightInsuranceNotificationRecord.builder()
                .eventKey(eventKey)
                .holder(holder)
                .policyId(policyId)
                .riskKey(purchaseNotification.map(FlightInsuranceNotificationRecord::getRiskKey).orElse(null))
                .type(TYPE_PAYOUT_DISTRIBUTED)
                .title("Claim Paid")
                .message(message)
                .amountWei(payoutWei)
                .flightNumber(flightNumber)
                .origin(origin)
                .destination(destination)
                .departureTime(purchaseNotification.map(FlightInsuranceNotificationRecord::getDepartureTime).orElse(null))
                .transactionHash(logEntry.getTransactionHash())
                .blockNumber(logEntry.getBlockNumber().longValueExact())
                .logIndex(logEntry.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .build());

        try {
            String payoutEth = formatWeiToEth(payoutWei);
            String departureAirportName = resolveAirportName(origin);
            String arrivalAirportName = resolveAirportName(destination);

            flightInsuranceNotificationService.queuePolicyResolution(
                    holder,
                    PolicyResolutionNotificationRequest.builder()
                            .policyId(String.valueOf(policyId))
                            .flightNumber(flightNumber)
                            .origin(origin)
                            .destination(destination)
                            .departureAirportName(departureAirportName)
                            .arrivalAirportName(arrivalAirportName)
                            .status("Payout Distributed")
                            .delayMinutes("See policy dashboard")
                            .payoutEth(payoutEth)
                            .resolutionTime(DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(eventTimestamp))
                            .transactionHash(logEntry.getTransactionHash())
                            .hasPayout(true)
                            .build()
            );
        } catch (Exception exception) {
            log.error("Failed to queue claim paid email for policy={}", policyId, exception);
        }
    }

    private void handleFlightStatusResolvedLog(Log logEntry, Map<String, Instant> blockTimestampCache) throws IOException {
        String eventKey = buildEventKey(TYPE_FLIGHT_RESOLVED, logEntry);
        if (flightInsuranceNotificationRecordDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        if (logEntry.getTopics().size() < 2) {
            return;
        }

        String riskKey = logEntry.getTopics().get(1);
        List<Type> values = FunctionReturnDecoder.decode(
                logEntry.getData(),
                FLIGHT_STATUS_RESOLVED_EVENT.getNonIndexedParameters()
        );
        int statusInt = ((Uint256) values.get(0)).getValue().intValue();
        int delayMinutes = ((Uint256) values.get(1)).getValue().intValue();
        FlightStatus status = statusInt < FlightStatus.values().length
                ? FlightStatus.values()[statusInt]
                : FlightStatus.Unknown;
        Instant eventTimestamp = blockTimestampCache.getOrDefault(logEntry.getBlockHash(), Instant.now());

        flightInsuranceNotificationRecordDAO.save(FlightInsuranceNotificationRecord.builder()
                .eventKey(eventKey)
                .riskKey(riskKey)
                .type(TYPE_FLIGHT_RESOLVED)
                .title("System Resolve")
                .message("Flight status resolution completed: " + status)
                .transactionHash(logEntry.getTransactionHash())
                .blockNumber(logEntry.getBlockNumber().longValueExact())
                .logIndex(logEntry.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .policyId(0L)
                .holder("0x0")
                .build());

        List<FlightInsuranceNotificationRecord> purchaseRecords =
                flightInsuranceNotificationRecordDAO.findByRiskKeyAndType(riskKey, TYPE_POLICY_PURCHASED);
        for (FlightInsuranceNotificationRecord purchase : purchaseRecords) {
            triggerResolutionEmail(purchase, status, delayMinutes, logEntry.getTransactionHash(), eventTimestamp);
        }
    }

    private void handleLiquidityProvidedLog(Log logEntry, Map<String, Instant> blockTimestampCache) throws IOException {
        String eventKey = buildEventKey(TYPE_LIQUIDITY_PROVIDED, logEntry);
        if (flightInsuranceNotificationRecordDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        if (logEntry.getTopics().size() < 2) {
            return;
        }

        String investor = decodeAddressTopic(logEntry.getTopics().get(1));
        List<Type> values = FunctionReturnDecoder.decode(
                logEntry.getData(),
                LIQUIDITY_PROVIDED_EVENT.getNonIndexedParameters()
        );
        String assetsWei = ((Uint256) values.get(0)).getValue().toString();
        String sharesMinted = ((Uint256) values.get(1)).getValue().toString();
        Instant eventTimestamp = blockTimestampCache.getOrDefault(logEntry.getBlockHash(), Instant.now());

        flightInsuranceNotificationRecordDAO.save(FlightInsuranceNotificationRecord.builder()
                .eventKey(eventKey)
                .holder(investor)
                .type(TYPE_LIQUIDITY_PROVIDED)
                .title("Liquidity Provided")
                .message("Successfully invested " + formatWeiToEth(assetsWei) + " ETH into the insurance pool.")
                .amountWei(assetsWei)
                .transactionHash(logEntry.getTransactionHash())
                .blockNumber(logEntry.getBlockNumber().longValueExact())
                .logIndex(logEntry.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .policyId(0L)
                .build());

        try {
            flightInsuranceNotificationService.queueInvestmentConfirmation(
                    investor,
                    formatWeiToEth(assetsWei),
                    formatWeiToEth(sharesMinted),
                    logEntry.getTransactionHash(),
                    eventTimestamp
            );
        } catch (Exception exception) {
            log.error("Failed to queue investment email for wallet={}", investor, exception);
        }
    }

    private void handleLiquidityWithdrawnLog(Log logEntry, Map<String, Instant> blockTimestampCache) throws IOException {
        String eventKey = buildEventKey(TYPE_LIQUIDITY_WITHDRAWN, logEntry);
        if (flightInsuranceNotificationRecordDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        if (logEntry.getTopics().size() < 2) {
            return;
        }

        String investor = decodeAddressTopic(logEntry.getTopics().get(1));
        List<Type> values = FunctionReturnDecoder.decode(
                logEntry.getData(),
                LIQUIDITY_WITHDRAWN_EVENT.getNonIndexedParameters()
        );
        String assetsWei = ((Uint256) values.get(0)).getValue().toString();
        String sharesBurned = ((Uint256) values.get(1)).getValue().toString();
        Instant eventTimestamp = blockTimestampCache.getOrDefault(logEntry.getBlockHash(), Instant.now());

        flightInsuranceNotificationRecordDAO.save(FlightInsuranceNotificationRecord.builder()
                .eventKey(eventKey)
                .holder(investor)
                .type(TYPE_LIQUIDITY_WITHDRAWN)
                .title("Liquidity Withdrawn")
                .message("Successfully withdrawn " + formatWeiToEth(assetsWei) + " ETH from the insurance pool.")
                .amountWei(assetsWei)
                .transactionHash(logEntry.getTransactionHash())
                .blockNumber(logEntry.getBlockNumber().longValueExact())
                .logIndex(logEntry.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .policyId(0L)
                .build());

        try {
            flightInsuranceNotificationService.queueWithdrawalConfirmation(
                    investor,
                    formatWeiToEth(assetsWei),
                    formatWeiToEth(sharesBurned),
                    logEntry.getTransactionHash(),
                    eventTimestamp
            );
        } catch (Exception exception) {
            log.error("Failed to queue withdrawal email for wallet={}", investor, exception);
        }
    }

    private void triggerResolutionEmail(
            FlightInsuranceNotificationRecord purchase,
            FlightStatus status,
            int delayMinutes,
            String transactionHash,
            Instant resolutionTime
    ) {
        Optional<FlightInsuranceNotificationRecord> payoutRecord = flightInsuranceNotificationRecordDAO
                .findFirstByPolicyIdAndTypeOrderByEventTimestampDescCreatedAtDesc(
                        purchase.getPolicyId(),
                        TYPE_PAYOUT_DISTRIBUTED
                );

        boolean hasPayout = payoutRecord.isPresent();
        String payoutEth = formatWeiToEth(payoutRecord.map(FlightInsuranceNotificationRecord::getAmountWei).orElse("0"));
        String departureAirportName = resolveAirportName(purchase.getOrigin());
        String arrivalAirportName = resolveAirportName(purchase.getDestination());

        try {
            flightInsuranceNotificationService.queuePolicyResolution(
                    purchase.getHolder(),
                    PolicyResolutionNotificationRequest.builder()
                            .policyId(String.valueOf(purchase.getPolicyId()))
                            .flightNumber(purchase.getFlightNumber())
                            .origin(purchase.getOrigin())
                            .destination(purchase.getDestination())
                            .departureAirportName(departureAirportName)
                            .arrivalAirportName(arrivalAirportName)
                            .status(status.name())
                            .delayMinutes(String.valueOf(delayMinutes))
                            .payoutEth(payoutEth)
                            .resolutionTime(DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(resolutionTime))
                            .transactionHash(transactionHash)
                            .hasPayout(hasPayout)
                            .build()
            );
        } catch (Exception exception) {
            log.error("Failed to queue resolution email for policy={}", purchase.getPolicyId(), exception);
        }
    }

    private String formatWeiToEth(String amountWei) {
        if (amountWei == null || amountWei.isBlank()) {
            return "0";
        }

        BigDecimal ethAmount = new BigDecimal(new BigInteger(amountWei))
                .divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return ethAmount.scale() < 0 ? ethAmount.setScale(0).toPlainString() : ethAmount.toPlainString();
    }

    private long resolveFromBlock() {
        return flightInsuranceNotificationSyncStateDAO.findById(SYNC_KEY)
                .map(state -> state.getLastProcessedBlock() + 1)
                .orElse(syncFromBlock);
    }

    private void saveSyncState(long latestBlock) {
        flightInsuranceNotificationSyncStateDAO.save(FlightInsuranceNotificationSyncState.builder()
                .syncKey(SYNC_KEY)
                .lastProcessedBlock(latestBlock)
                .updatedAt(Instant.now())
                .build());
    }

    private String buildEventKey(String eventType, Log logEntry) {
        return eventType + ":" + logEntry.getTransactionHash() + ":" + logEntry.getLogIndex();
    }

    private BigInteger decodeUint256Topic(String topic) {
        return new BigInteger(stripHexPrefix(topic), 16);
    }

    private String decodeAddressTopic(String topic) {
        String normalized = stripHexPrefix(topic);
        return "0x" + normalized.substring(normalized.length() - 40);
    }

    private String stripHexPrefix(String value) {
        return value != null && value.startsWith("0x") ? value.substring(2) : value;
    }

    private String hexTopic(String topic) {
        return Numeric.prependHexPrefix(stripHexPrefix(topic));
    }

    private String normalizeContractAddress(String address) {
        return Numeric.prependHexPrefix(stripHexPrefix(address == null ? "" : address.trim()));
    }

    private Set<String> trackedTopics() {
        return new LinkedHashSet<>(List.of(
                hexTopic(EventEncoder.encode(POLICY_PURCHASED_EVENT)),
                hexTopic(EventEncoder.encode(AUTO_PAYOUT_TRIGGERED_EVENT)),
                hexTopic(EventEncoder.encode(FLIGHT_STATUS_RESOLVED_EVENT)),
                hexTopic(EventEncoder.encode(LIQUIDITY_PROVIDED_EVENT)),
                hexTopic(EventEncoder.encode(LIQUIDITY_WITHDRAWN_EVENT))
        ));
    }

    private String buildPayoutMessage(Long policyId, String flightNumber, String origin, String destination) {
        if (origin == null || destination == null) {
            return "Policy #" + policyId + " paid out successfully.";
        }

        return flightNumber + " " + origin + " -> " + destination + " paid out successfully.";
    }

    private String resolveAirportName(String iataCode) {
        if (iataCode == null || iataCode.isBlank()) {
            return iataCode;
        }

        try {
            Airport airport = airportDAO.findFirstByIataCode(iataCode.toUpperCase(Locale.ROOT));
            if (airport != null && airport.getName() != null && !airport.getName().isBlank()) {
                return airport.getName();
            }
        } catch (Exception exception) {
            log.warn("Failed to resolve airport name for IATA code: {}", iataCode);
        }

        return iataCode;
    }
}
