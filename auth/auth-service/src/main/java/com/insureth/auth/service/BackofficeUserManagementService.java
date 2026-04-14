package com.insureth.auth.service;

import com.insureth.auth.domain.entity.BackofficeUser;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.BackofficeUserDAO;
import com.insureth.auth.domain.repository.UserDAO;
import com.insureth.auth.model.dto.BackofficeUserCreateRequest;
import com.insureth.auth.model.dto.BackofficeUserModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackofficeUserManagementService {

    private final BackofficeUserDAO backofficeUserDAO;
    private final UserDAO userDAO;
    private final AuthAuditTrailService authAuditTrailService;

    @Transactional(readOnly = true)
    public List<BackofficeUserModel> listUsers() {
        return backofficeUserDAO.findAll()
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Transactional
    public BackofficeUserModel createUser(
            BackofficeUserCreateRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        String normalizedWalletAddress = request.getWalletAddress().trim();
        if (backofficeUserDAO.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BadCredentialsException("Username is already in use");
        }

        if (backofficeUserDAO.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadCredentialsException("Email is already in use");
        }

        if (backofficeUserDAO.existsByUserWalletAddressIgnoreCase(normalizedWalletAddress)) {
            throw new BadCredentialsException("Wallet is already authorized for backoffice access");
        }

        User identityUser = userDAO.findByWalletAddressIgnoreCase(normalizedWalletAddress)
                .orElseGet(() -> userDAO.save(User.builder()
                        .walletAddress(normalizedWalletAddress)
                        .active(true)
                        .build()));

        BackofficeUser backofficeUser = BackofficeUser.builder()
                .user(identityUser)
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .role(request.getRole().trim().toUpperCase())
                .build();

        BackofficeUser saved = backofficeUserDAO.save(backofficeUser);
        authAuditTrailService.record(
                "BACKOFFICE_USER_CREATED",
                actorWalletAddress,
                actorRole,
                "BACKOFFICE_USER",
                String.valueOf(saved.getUserId()),
                "Created backoffice user " + saved.getUsername(),
                ipAddress,
                userAgent
        );
        return toModel(saved);
    }

    private BackofficeUserModel toModel(BackofficeUser user) {
        return BackofficeUserModel.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .walletAddress(user.getUser().getWalletAddress())
                .role(user.getRole())
                .build();
    }
}
