package com.insureth.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackofficeUserModel {
    private Long userId;
    private String username;
    private String email;
    private String walletAddress;
    private String role;
}
