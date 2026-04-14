package com.insureth.auth.service;

import com.insureth.auth.domain.entity.ClientPortalUser;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.ClientPortalUserDAO;
import com.insureth.auth.domain.repository.UserDAO;
import com.insureth.auth.model.dto.ClientUserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientPortalUserService {

    private final ClientPortalUserDAO clientPortalUserDAO;
    private final UserDAO userDAO;

    public boolean checkIfUserExists(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        return clientPortalUserDAO.existsByUserWalletAddressIgnoreCase(walletAddress);
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

