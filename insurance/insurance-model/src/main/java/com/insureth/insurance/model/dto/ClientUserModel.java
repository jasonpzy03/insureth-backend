package com.insureth.insurance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local representation of a Client Portal User, shadowed from the Auth service 
 * to decouple microservice builds.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientUserModel {
    private String walletAddress;
    private String username;
    private String email;
}
