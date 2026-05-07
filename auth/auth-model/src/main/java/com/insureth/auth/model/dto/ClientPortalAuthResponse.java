package com.insureth.auth.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalAuthResponse {
    private String token;
    private Instant expiresAt;
    private boolean profileComplete;
    private String walletAddress;
}
