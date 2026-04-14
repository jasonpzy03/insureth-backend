package com.insureth.auth.service;

import com.insureth.auth.domain.entity.BackofficeUser;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.BackofficeUserDAO;
import com.insureth.auth.domain.repository.UserDAO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackofficeUserBootstrapService {

    private final BackofficeUserDAO backofficeUserDAO;
    private final UserDAO userDAO;

    @Value("${backoffice.seed.username:admin}")
    private String username;

    @Value("${backoffice.seed.email:admin@insureth.local}")
    private String email;

    @Value("${backoffice.seed.wallet-address:0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266}")
    private String walletAddress;

    @PostConstruct
    public void ensureSeedUser() {
        if (backofficeUserDAO.existsByUsernameIgnoreCase(username)
                || backofficeUserDAO.existsByEmailIgnoreCase(email)
                || backofficeUserDAO.existsByUserWalletAddressIgnoreCase(walletAddress)) {
            return;
        }

        User identityUser = userDAO.findByWalletAddressIgnoreCase(walletAddress)
                .orElseGet(() -> userDAO.save(User.builder()
                        .walletAddress(walletAddress)
                        .active(true)
                        .build()));

        BackofficeUser backofficeUser = BackofficeUser.builder()
                .user(identityUser)
                .username(username)
                .email(email)
                .role("ADMIN")
                .build();

        backofficeUserDAO.save(backofficeUser);
    }
}
