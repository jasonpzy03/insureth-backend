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
public class BackofficeNonceResponse {
    private String nonce;
    private Instant expiresAt;
    private String message;
}
