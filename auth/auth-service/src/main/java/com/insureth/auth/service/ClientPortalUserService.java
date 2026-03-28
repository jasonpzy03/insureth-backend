package com.insureth.auth.service;

import com.insureth.auth.domain.entity.ClientPortalUser;
import com.insureth.auth.domain.repository.ClientPortalUserDAO;
import com.insureth.auth.model.dto.ClientUserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientPortalUserService {

    private final ClientPortalUserDAO clientPortalUserDAO;

    public boolean checkIfUserExists(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        return clientPortalUserDAO.existsByWalletAddressIgnoreCase(walletAddress);
    }

    public void createUser(ClientUserModel userModel) {
        ClientPortalUser clientPortalUser = new ClientPortalUser();
        clientPortalUser.setEmail(userModel.getEmail());
        clientPortalUser.setUsername(userModel.getUsername());
        clientPortalUser.setWalletAddress(userModel.getWalletAddress());
        clientPortalUserDAO.save(clientPortalUser);
    }
}

