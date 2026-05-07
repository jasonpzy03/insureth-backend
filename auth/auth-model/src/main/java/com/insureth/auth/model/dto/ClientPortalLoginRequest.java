package com.insureth.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalLoginRequest {
    @NotBlank
    private String walletAddress;

    @NotBlank
    private String signature;

    @NotBlank
    private String nonce;

    @NotBlank
    private String message;
}
