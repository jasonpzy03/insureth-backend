package com.insureth.auth.service;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

@Service
public class WalletSignatureVerifier {

    public boolean matches(String expectedWalletAddress, String nonce, String message, String signature) {
        return matchesSiweMessage(expectedWalletAddress, nonce, message, signature);
    }

    public boolean matchesClientPortal(String expectedWalletAddress, String nonce, String message, String signature) {
        return matchesSiweMessage(expectedWalletAddress, nonce, message, signature);
    }

    public boolean matchesExpectedFields(
            String message,
            String expectedWalletAddress,
            String expectedNonce,
            String expectedDomain,
            String expectedUri,
            long expectedChainId,
            Instant expectedIssuedAt,
            String expectedStatement
    ) {
        try {
            ParsedSiweMessage parsed = parseSiweMessage(message);
            return parsed.address().equalsIgnoreCase(expectedWalletAddress)
                    && parsed.nonce().equals(expectedNonce)
                    && parsed.domain().equalsIgnoreCase(normalizeExpectedDomain(expectedDomain))
                    && parsed.uri().equals(expectedUri)
                    && parsed.chainId() == expectedChainId
                    && parsed.issuedAt().equals(expectedIssuedAt)
                    && parsed.statement().equals(expectedStatement);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean matchesSiweMessage(String expectedWalletAddress, String expectedNonce, String message, String signature) {
        try {
            ParsedSiweMessage parsed = parseSiweMessage(message);
            if (!parsed.address().equalsIgnoreCase(expectedWalletAddress)) {
                return false;
            }

            if (!parsed.nonce().equals(expectedNonce)) {
                return false;
            }

            String recovered = recoverAddress(message, signature);
            return recovered != null && recovered.equalsIgnoreCase(expectedWalletAddress);
        } catch (Exception ex) {
            return false;
        }
    }

    private ParsedSiweMessage parseSiweMessage(String message) {
        String normalized = message.replace("\r\n", "\n").trim();
        String[] lines = normalized.split("\n");
        if (lines.length < 8) {
            throw new IllegalArgumentException("Invalid SIWE message");
        }

        String header = lines[0];
        String suffix = " wants you to sign in with your Ethereum account:";
        if (!header.endsWith(suffix)) {
            throw new IllegalArgumentException("SIWE header missing");
        }

        String authority = header.substring(0, header.length() - suffix.length()).trim();
        String address = lines[1].trim();
        if (authority.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("SIWE domain or address missing");
        }

        Map<String, String> fields = new HashMap<>();
        for (String line : lines) {
            int separator = line.indexOf(": ");
            if (separator <= 0) {
                continue;
            }

            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 2).trim();
            fields.put(key, value);
        }

        String uriValue = requireField(fields, "URI");
        String version = requireField(fields, "Version");
        String chainIdValue = requireField(fields, "Chain ID");
        String nonce = requireField(fields, "Nonce");
        String issuedAtValue = requireField(fields, "Issued At");
        String statement = extractStatement(lines);

        if (!"1".equals(version)) {
            throw new IllegalArgumentException("Unsupported SIWE version");
        }

        long chainId = Long.parseLong(chainIdValue);
        if (chainId <= 0) {
            throw new IllegalArgumentException("Invalid SIWE chain ID");
        }

        Instant issuedAt;
        try {
            issuedAt = Instant.parse(issuedAtValue);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid SIWE issued-at");
        }

        URI uri = URI.create(uriValue);
        String normalizedAuthority = authority;
        String normalizedScheme = null;
        if (authority.contains("://")) {
            URI authorityUri = URI.create(authority);
            normalizedScheme = authorityUri.getScheme();
            normalizedAuthority = authorityUri.getAuthority();
        }

        if (!uri.isAbsolute()
                || uri.getAuthority() == null
                || normalizedAuthority == null
                || !uri.getAuthority().equalsIgnoreCase(normalizedAuthority)) {
            throw new IllegalArgumentException("SIWE URI does not match domain");
        }

        if (normalizedScheme != null && !uri.getScheme().equalsIgnoreCase(normalizedScheme)) {
            throw new IllegalArgumentException("SIWE URI scheme does not match header scheme");
        }

        return new ParsedSiweMessage(normalizedAuthority, address, uriValue, nonce, issuedAt, chainId, statement);
    }

    private String extractStatement(String[] lines) {
        if (lines.length < 5) {
            return "";
        }

        String statement = lines[3].trim();
        if (statement.isEmpty() || statement.contains(": ")) {
            return "";
        }

        return statement;
    }

    private String requireField(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing SIWE field: " + key);
        }
        return value;
    }

    private String normalizeExpectedDomain(String expectedDomain) {
        if (expectedDomain == null || expectedDomain.isBlank()) {
            return expectedDomain;
        }

        if (expectedDomain.contains("://")) {
            URI uri = URI.create(expectedDomain);
            return uri.getAuthority() != null ? uri.getAuthority() : expectedDomain;
        }

        return expectedDomain;
    }

    private String recoverAddress(String message, String signatureHex) throws SignatureException {
        byte[] signature = Numeric.hexStringToByteArray(signatureHex);
        if (signature.length != 65) {
            return null;
        }

        byte v = signature[64];
        if (v < 27) {
            v += 27;
        }

        Sign.SignatureData signatureData = new Sign.SignatureData(
                v,
                Arrays.copyOfRange(signature, 0, 32),
                Arrays.copyOfRange(signature, 32, 64)
        );

        BigInteger publicKey = Sign.signedPrefixedMessageToKey(
                message.getBytes(StandardCharsets.UTF_8),
                signatureData
        );

        return "0x" + Keys.getAddress(publicKey);
    }

    private record ParsedSiweMessage(
            String domain,
            String address,
            String uri,
            String nonce,
            Instant issuedAt,
            long chainId,
            String statement
    ) {
    }
}
