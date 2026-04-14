package com.insureth.auth.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackofficeUserCreateRequest {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String walletAddress;

    @NotBlank
    private String role;
}
