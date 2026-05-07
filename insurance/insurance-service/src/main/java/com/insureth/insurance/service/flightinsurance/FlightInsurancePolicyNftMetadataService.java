package com.insureth.insurance.service.flightinsurance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insureth.insurance.domain.entity.FlightInsurancePolicyNftSyncState;
import com.insureth.insurance.domain.repository.FlightInsurancePolicyNftSyncStateDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightInsurancePolicyNftMetadataService {

    private static final String PINATA_ENDPOINT = "https://api.pinata.cloud/pinning/pinJSONToIPFS";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_UPLOADED = "UPLOADED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final DateTimeFormatter ISO_UTC_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private final Web3j web3j;
    private final ObjectMapper objectMapper;
    private final FlightInsurancePolicyNftSyncStateDAO flightInsurancePolicyNftSyncStateDAO;

    @Value("${flight-insurance.contract-address:}")
    private String contractAddress;

    @Value("${flight-insurance.nft.enabled:false}")
    private boolean nftSyncEnabled;

    @Value("${flight-insurance.nft.pinata.jwt:}")
    private String pinataJwt;

    @Value("${flight-insurance.nft.owner-private-key:}")
    private String ownerPrivateKey;

    @Value("${flight-insurance.nft.image-cid:}")
    private String imageCid;

    @Value("${flight-insurance.nft.image-mime-type:image/png}")
    private String imageMimeType;

    @Value("${flight-insurance.nft.collection-name:Insureth Flight Policy}")
    private String collectionName;

    @Value("${flight-insurance.nft.policy-base-url:}")
    private String policyBaseUrl;

    @Value("${flight-insurance.nft.gas-limit:350000}")
    private long gasLimit;

    @Value("${flight-insurance.nft.receipt-timeout-ms:120000}")
    private long receiptTimeoutMs;

    @Value("${flight-insurance.nft.receipt-poll-interval-ms:3000}")
    private long receiptPollIntervalMs;

    @Transactional
    public void syncPolicyMetadata(
            long policyId,
            String holder,
            String riskKey,
            String flightNumber,
            String origin,
            String destination,
            long departureTime,
            String premiumWei
    ) {
        if (!nftSyncEnabled) {
            return;
        }

        validateConfiguration();

        Optional<FlightInsurancePolicyNftSyncState> existingState = flightInsurancePolicyNftSyncStateDAO.findById(policyId);
        if (existingState.isPresent()
                && STATUS_COMPLETED.equalsIgnoreCase(existingState.get().getStatus())
                && existingState.get().getTokenUri() != null
                && !existingState.get().getTokenUri().isBlank()) {
            return;
        }

        Instant now = Instant.now();
        FlightInsurancePolicyNftSyncState state = existingState.orElseGet(() -> FlightInsurancePolicyNftSyncState.builder()
                .policyId(policyId)
                .holder(holder)
                .riskKey(riskKey)
                .createdAt(now)
                .build());
        state.setHolder(holder);
        state.setRiskKey(riskKey);
        state.setStatus(STATUS_PENDING);
        state.setFailureReason(null);
        state.setUpdatedAt(now);
        flightInsurancePolicyNftSyncStateDAO.save(state);

        try {
            Map<String, Object> metadata = buildMetadata(
                    policyId,
                    holder,
                    riskKey,
                    flightNumber,
                    origin,
                    destination,
                    departureTime,
                    premiumWei
            );
            String metadataCid = uploadMetadataToPinata(policyId, metadata);
            String tokenUri = "ipfs://" + metadataCid;

            state.setMetadataCid(metadataCid);
            state.setTokenUri(tokenUri);
            state.setStatus(STATUS_UPLOADED);
            state.setUpdatedAt(Instant.now());
            flightInsurancePolicyNftSyncStateDAO.save(state);

            TransactionReceipt receipt = updateOnChainTokenUri(policyId, tokenUri);
            state.setContractUpdateTxHash(receipt.getTransactionHash());
            state.setStatus(STATUS_COMPLETED);
            state.setUpdatedAt(Instant.now());
            flightInsurancePolicyNftSyncStateDAO.save(state);
        } catch (Exception exception) {
            state.setStatus(STATUS_FAILED);
            state.setFailureReason(truncate(exception.getMessage(), 1000));
            state.setUpdatedAt(Instant.now());
            flightInsurancePolicyNftSyncStateDAO.save(state);
            throw new IllegalStateException("Failed to sync NFT metadata for policy #" + policyId, exception);
        }
    }

    private void validateConfiguration() {
        if (contractAddress == null || contractAddress.isBlank()) {
            throw new IllegalStateException("flight-insurance.contract-address is not configured");
        }
        if (pinataJwt == null || pinataJwt.isBlank()) {
            throw new IllegalStateException("flight-insurance.nft.pinata.jwt is not configured");
        }
        if (ownerPrivateKey == null || ownerPrivateKey.isBlank()) {
            throw new IllegalStateException("flight-insurance.nft.owner-private-key is not configured");
        }
        if (imageCid == null || imageCid.isBlank()) {
            throw new IllegalStateException("flight-insurance.nft.image-cid is not configured");
        }
    }

    private Map<String, Object> buildMetadata(
            long policyId,
            String holder,
            String riskKey,
            String flightNumber,
            String origin,
            String destination,
            long departureTime,
            String premiumWei
    ) {
        String route = origin + " -> " + destination;
        String displayName = "Insureth Flight Policy #" + policyId;
        String description = "Soulbound parametric flight insurance policy issued by Insureth for "
                + flightNumber + " on route " + route + ".";

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", displayName);
        metadata.put("description", description);
        metadata.put("image", buildIpfsAssetUri(imageCid));
        metadata.put("image_mime_type", imageMimeType);
        metadata.put("external_url", buildPolicyExternalUrl(policyId));
        metadata.put("background_color", "F5F7FB");
        metadata.put("attributes", List.of(
                attribute("Policy ID", String.valueOf(policyId)),
                attribute("Policy Type", "Flight Delay Insurance"),
                attribute("Flight Number", flightNumber),
                attribute("Route", route),
                attribute("Origin", origin),
                attribute("Destination", destination),
                attribute("Departure Time UTC", ISO_UTC_FORMATTER.format(Instant.ofEpochSecond(departureTime))),
                attribute("Premium ETH", formatWeiToEth(premiumWei)),
                attribute("Holder", holder),
                attribute("Risk Key", riskKey),
                attribute("Collection", collectionName),
                attribute("Transferability", "Soulbound")
        ));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("policyId", policyId);
        properties.put("riskKey", riskKey);
        properties.put("holder", holder);
        properties.put("flightNumber", flightNumber);
        properties.put("origin", origin);
        properties.put("destination", destination);
        properties.put("departureTimeUtc", Instant.ofEpochSecond(departureTime).toString());
        properties.put("premiumWei", premiumWei);
        properties.put("premiumEth", formatWeiToEth(premiumWei));
        properties.put("tokenStandard", "ERC-721 Soulbound");
        properties.put("issuer", "Insureth");
        metadata.put("properties", properties);

        return metadata;
    }

    private Map<String, Object> attribute(String traitType, String value) {
        Map<String, Object> attribute = new LinkedHashMap<>();
        attribute.put("trait_type", traitType);
        attribute.put("value", value);
        return attribute;
    }

    private String uploadMetadataToPinata(long policyId, Map<String, Object> metadata) throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pinataMetadata", Map.of("name", "insureth-policy-" + policyId + ".json"));
        payload.put("pinataContent", metadata);

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PINATA_ENDPOINT))
                .header("Authorization", "Bearer " + pinataJwt)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Pinata upload failed with status " + response.statusCode() + ": " + response.body());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Object cid = responseBody.get("IpfsHash");
        if (!(cid instanceof String ipfsHash) || ipfsHash.isBlank()) {
            throw new IllegalStateException("Pinata upload succeeded but no IpfsHash was returned");
        }

        return ipfsHash;
    }

    private TransactionReceipt updateOnChainTokenUri(long policyId, String tokenUri) throws Exception {
        Credentials credentials = Credentials.create(ownerPrivateKey);
        BigInteger signerBalanceWei = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();
        long chainId = web3j.ethChainId().send().getChainId().longValueExact();
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

        log.info(
                "NFT metadata signer address={}, chainId={}, balanceWei={}",
                credentials.getAddress(),
                chainId,
                signerBalanceWei
        );

        RawTransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);
        Function function = new Function(
                "setPolicyTokenURI",
                List.<Type>of(new Uint256(BigInteger.valueOf(policyId)), new Utf8String(tokenUri)),
                List.of()
        );

        EthSendTransaction sendTransactionResponse = transactionManager.sendTransaction(
                gasPrice,
                BigInteger.valueOf(gasLimit),
                normalizeContractAddress(contractAddress),
                FunctionEncoder.encode(function),
                BigInteger.ZERO
        );

        if (sendTransactionResponse.hasError()) {
            throw new IllegalStateException(
                    "On-chain metadata update RPC error: code="
                            + sendTransactionResponse.getError().getCode()
                            + ", message="
                            + sendTransactionResponse.getError().getMessage()
            );
        }

        String transactionHash = sendTransactionResponse.getTransactionHash();

        if (transactionHash == null || transactionHash.isBlank()) {
            throw new IllegalStateException(
                    "On-chain metadata update did not return a transaction hash and no RPC error was provided"
            );
        }

        TransactionReceipt minedReceipt = waitForReceipt(transactionHash);
        if (!"0x1".equalsIgnoreCase(minedReceipt.getStatus())) {
            throw new IllegalStateException("On-chain metadata update failed for tx " + transactionHash);
        }

        return minedReceipt;
    }

    private TransactionReceipt waitForReceipt(String transactionHash) throws Exception {
        long startedAt = System.currentTimeMillis();

        while (System.currentTimeMillis() - startedAt < receiptTimeoutMs) {
            Optional<TransactionReceipt> receipt = web3j.ethGetTransactionReceipt(transactionHash)
                    .send()
                    .getTransactionReceipt();
            if (receipt.isPresent()) {
                return receipt.get();
            }

            Thread.sleep(receiptPollIntervalMs);
        }

        throw new IllegalStateException("Timed out waiting for metadata update transaction receipt " + transactionHash);
    }

    private String buildPolicyExternalUrl(long policyId) {
        if (policyBaseUrl == null || policyBaseUrl.isBlank()) {
            return null;
        }
        return policyBaseUrl.endsWith("/")
                ? policyBaseUrl + "policies/" + policyId
                : policyBaseUrl + "/policies/" + policyId;
    }

    private String buildIpfsAssetUri(String cid) {
        return "ipfs://" + cid;
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

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizeContractAddress(String address) {
        return Numeric.prependHexPrefix(address == null ? "" : Numeric.cleanHexPrefix(address.trim()));
    }
}
