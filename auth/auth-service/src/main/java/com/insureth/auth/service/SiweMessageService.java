package com.insureth.auth.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiweMessageService {

    @Value("${auth.siwe.client-portal.domain:http://localhost:4200}")
    private String clientPortalDomain;

    @Value("${auth.siwe.client-portal.uri:http://localhost:4200}")
    private String clientPortalUri;

    @Value("${auth.siwe.client-portal.statement:Sign in to Insureth Client Portal}")
    private String clientPortalStatement;

    @Value("${auth.siwe.client-portal.chain-id:31337}")
    private long clientPortalChainId;

    @Value("${auth.siwe.backoffice.domain:http://localhost:4300}")
    private String backofficeDomain;

    @Value("${auth.siwe.backoffice.uri:http://localhost:4300}")
    private String backofficeUri;

    @Value("${auth.siwe.backoffice.statement:Sign in to Insureth Back Office}")
    private String backofficeStatement;

    @Value("${auth.siwe.backoffice.chain-id:31337}")
    private long backofficeChainId;

    public String buildClientPortalMessage(String walletAddress, String nonce, Instant issuedAt) {
        return buildMessage(clientPortalDomain, walletAddress, clientPortalStatement, clientPortalUri, clientPortalChainId, nonce, issuedAt);
    }

    public String buildBackofficeMessage(String walletAddress, String nonce, Instant issuedAt) {
        return buildMessage(backofficeDomain, walletAddress, backofficeStatement, backofficeUri, backofficeChainId, nonce, issuedAt);
    }

    public String getClientPortalDomain() {
        return clientPortalDomain;
    }

    public String getClientPortalUri() {
        return clientPortalUri;
    }

    public String getClientPortalStatement() {
        return clientPortalStatement;
    }

    public long getClientPortalChainId() {
        return clientPortalChainId;
    }

    public String getBackofficeDomain() {
        return backofficeDomain;
    }

    public String getBackofficeUri() {
        return backofficeUri;
    }

    public String getBackofficeStatement() {
        return backofficeStatement;
    }

    public long getBackofficeChainId() {
        return backofficeChainId;
    }

    private String buildMessage(
            String domain,
            String walletAddress,
            String statement,
            String uri,
            long chainId,
            String nonce,
            Instant issuedAt
    ) {
        return domain + " wants you to sign in with your Ethereum account:\n"
                + walletAddress + "\n\n"
                + statement + "\n\n"
                + "URI: " + uri + "\n"
                + "Version: 1\n"
                + "Chain ID: " + chainId + "\n"
                + "Nonce: " + nonce + "\n"
                + "Issued At: " + issuedAt.toString();
    }
}
