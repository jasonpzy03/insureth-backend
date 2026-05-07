package com.insureth.insurance.service.client.auth;

import com.insureth.insurance.model.dto.ClientUserModel;
import com.insureth.insurance.service.client.auth.config.AuthUserFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "AuthUserApiFeign",
        url = "${auth.api.url}",
        configuration = AuthUserFeignConfig.class
)
public interface AuthUserApiFeign {

    @GetMapping("/api/v1/auth/internal/client-users")
    ClientUserModel getClientUserByWalletAddress(@RequestParam("walletAddress") String walletAddress);
}
