package com.insureth.auth.service;

import com.insureth.auth.domain.entity.ClientPortalUser;
import com.insureth.auth.domain.entity.PendingClientPortalUser;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.ClientPortalUserDAO;
import com.insureth.auth.domain.repository.PendingClientPortalUserDAO;
import com.insureth.auth.domain.repository.UserDAO;
import com.insureth.auth.model.dto.ClientUserModel;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientPortalUserService {

    private final ClientPortalUserDAO clientPortalUserDAO;
    private final UserDAO userDAO;
    private final PendingClientPortalUserDAO pendingClientPortalUserDAO;
    private final JmsTemplate jmsTemplate;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public boolean checkIfUserExists(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        return clientPortalUserDAO.existsByUserWalletAddressIgnoreCase(walletAddress);
    }

    @Transactional
    public void initiateSignup(ClientUserModel userModel) {
        if (checkIfUserExists(userModel.getWalletAddress())) {
            throw new IllegalArgumentException("Wallet address is already registered");
        }
        if (clientPortalUserDAO.existsByUsernameIgnoreCase(userModel.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (clientPortalUserDAO.existsByEmailIgnoreCase(userModel.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String token = UUID.randomUUID().toString();

        pendingClientPortalUserDAO.deleteByWalletAddressIgnoreCase(userModel.getWalletAddress());

        PendingClientPortalUser pendingUser = PendingClientPortalUser
                .builder()
                .token(token)
                .walletAddress(userModel.getWalletAddress())
                .username(userModel.getUsername())
                .email(userModel.getEmail())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        pendingClientPortalUserDAO.save(pendingUser);

        // Send email message via JMS
        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", userModel.getEmail());
        emailRequest.put("subject", "Verify Your Insureth Account");
        emailRequest.put("template", "verify-email.ftl");
        emailRequest.put("username", userModel.getUsername());
        emailRequest.put("verificationLink", frontendUrl + "/auth/verify?token=" + token);

        jmsTemplate.convertAndSend("email-queue", emailRequest);
    }

    @Transactional
    public void verifySignup(String token) {
        PendingClientPortalUser pendingUser = pendingClientPortalUserDAO.findById(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (pendingUser.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingClientPortalUserDAO.delete(pendingUser);
            throw new IllegalArgumentException("Verification token has expired");
        }

        ClientUserModel userModel = new ClientUserModel();
        userModel.setWalletAddress(pendingUser.getWalletAddress());
        userModel.setUsername(pendingUser.getUsername());
        userModel.setEmail(pendingUser.getEmail());

        // This validates and inserts into the main table
        createUser(userModel);

        // Cleanup pending
        pendingClientPortalUserDAO.delete(pendingUser);
    }

    @Transactional
    public void createUser(ClientUserModel userModel) {
        if (clientPortalUserDAO.existsByUsernameIgnoreCase(userModel.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (clientPortalUserDAO.existsByEmailIgnoreCase(userModel.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = userDAO.findByWalletAddressIgnoreCase(userModel.getWalletAddress())
                .orElseGet(() -> userDAO.save(User.builder()
                        .walletAddress(userModel.getWalletAddress())
                        .active(true)
                        .build()));

        ClientPortalUser clientPortalUser = new ClientPortalUser();
        clientPortalUser.setUser(user);
        clientPortalUser.setEmail(userModel.getEmail());
        clientPortalUser.setUsername(userModel.getUsername());
        clientPortalUserDAO.save(clientPortalUser);
    }

    public ClientUserModel getUserByWalletAddress(String walletAddress) {
        ClientPortalUser entity = clientPortalUserDAO.findByUserWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ClientUserModel model = new ClientUserModel();
        model.setUsername(entity.getUsername());
        model.setEmail(entity.getEmail());
        model.setWalletAddress(entity.getUser().getWalletAddress());
        return model;
    }

    @Transactional
    public void updateUser(String walletAddress, ClientUserModel model) {
        ClientPortalUser entity = clientPortalUserDAO.findByUserWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (clientPortalUserDAO.existsByUsernameIgnoreCaseAndUserUserIdNot(model.getUsername(), entity.getUserId())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (clientPortalUserDAO.existsByEmailIgnoreCaseAndUserUserIdNot(model.getEmail(), entity.getUserId())) {
            throw new IllegalArgumentException("Email already exists");
        }

        entity.setUsername(model.getUsername());
        entity.setEmail(model.getEmail());
        clientPortalUserDAO.save(entity);
    }
}
