package com.insureth.auth.service;

import com.insureth.auth.domain.entity.BackofficeRole;
import com.insureth.auth.domain.entity.BackofficeUser;
import com.insureth.auth.domain.entity.BackofficeUserRole;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.BackofficeRoleDAO;
import com.insureth.auth.domain.repository.BackofficeUserDAO;
import com.insureth.auth.domain.repository.BackofficeUserRoleDAO;
import com.insureth.auth.domain.repository.UserDAO;
import com.insureth.auth.model.dto.BackofficeUserCreateRequest;
import com.insureth.auth.model.dto.BackofficeRoleModel;
import com.insureth.auth.model.dto.BackofficeUserModel;
import com.insureth.auth.model.dto.BackofficeUserRolesUpdateRequest;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackofficeUserManagementService {

    private final BackofficeRoleDAO backofficeRoleDAO;
    private final BackofficeUserDAO backofficeUserDAO;
    private final BackofficeUserRoleDAO backofficeUserRoleDAO;
    private final UserDAO userDAO;
    private final AuthAuditTrailService authAuditTrailService;
    private final BackofficeAuthorizationService backofficeAuthorizationService;

    @Transactional(readOnly = true)
    public List<BackofficeUserModel> listUsers() {
        return backofficeUserDAO.findAll()
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackofficeRoleModel> listRoles() {
        return backofficeRoleDAO.findAll().stream()
                .sorted(Comparator.comparing(BackofficeRole::getName))
                .map(role -> BackofficeRoleModel.builder()
                        .roleId(role.getRoleId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .rights(role.getRoleRights().stream()
                                .map(roleRight -> roleRight.getRightCode())
                                .sorted()
                                .toList())
                        .build())
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

        List<String> requestedRoleNames = resolveRequestedRoleNames(request);
        List<BackofficeRole> assignedRoles = requestedRoleNames.stream()
                .map(roleName -> backofficeRoleDAO.findByNameIgnoreCase(roleName)
                        .orElseThrow(() -> new BadCredentialsException("Unknown backoffice role: " + roleName)))
                .toList();

        User identityUser = userDAO.findByWalletAddressIgnoreCase(normalizedWalletAddress)
                .orElseGet(() -> userDAO.save(User.builder()
                        .walletAddress(normalizedWalletAddress)
                        .active(true)
                        .build()));

        BackofficeUser backofficeUser = BackofficeUser.builder()
                .user(identityUser)
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .build();

        BackofficeUser saved = backofficeUserDAO.save(backofficeUser);
        List<BackofficeUserRole> userRoles = assignedRoles.stream()
                .map(role -> BackofficeUserRole.builder()
                        .user(saved)
                        .role(role)
                        .build())
                .toList();
        backofficeUserRoleDAO.saveAll(userRoles);
        saved.getUserRoles().addAll(userRoles);

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

    @Transactional
    public BackofficeUserModel updateUserRoles(
            Long userId,
            BackofficeUserRolesUpdateRequest request,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        BackofficeUser backofficeUser = backofficeUserDAO.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Backoffice user not found"));

        List<String> requestedRoleNames = resolveRequestedRoleNames(request.getRoles());
        List<BackofficeRole> assignedRoles = requestedRoleNames.stream()
                .map(roleName -> backofficeRoleDAO.findByNameIgnoreCase(roleName)
                        .orElseThrow(() -> new BadCredentialsException("Unknown backoffice role: " + roleName)))
                .toList();

        Map<String, BackofficeUserRole> existingRolesByName = backofficeUser.getUserRoles().stream()
                .collect(LinkedHashMap::new,
                        (accumulator, userRole) -> accumulator.put(userRole.getRole().getName().toUpperCase(Locale.ROOT), userRole),
                        Map::putAll);

        List<BackofficeUserRole> rolesToRemove = backofficeUser.getUserRoles().stream()
                .filter(userRole -> !requestedRoleNames.contains(userRole.getRole().getName().toUpperCase(Locale.ROOT)))
                .toList();

        if (!rolesToRemove.isEmpty()) {
            backofficeUser.getUserRoles().removeAll(rolesToRemove);
            backofficeUserRoleDAO.deleteAll(rolesToRemove);
        }

        List<BackofficeUserRole> rolesToAdd = assignedRoles.stream()
                .filter(role -> !existingRolesByName.containsKey(role.getName().toUpperCase(Locale.ROOT)))
                .map(role -> BackofficeUserRole.builder()
                        .user(backofficeUser)
                        .role(role)
                        .build())
                .toList();

        if (!rolesToAdd.isEmpty()) {
            backofficeUserRoleDAO.saveAll(rolesToAdd);
            backofficeUser.getUserRoles().addAll(rolesToAdd);
        }

        authAuditTrailService.record(
                "BACKOFFICE_USER_UPDATED",
                actorWalletAddress,
                actorRole,
                "BACKOFFICE_USER",
                String.valueOf(backofficeUser.getUserId()),
                "Updated backoffice roles for " + backofficeUser.getUsername() + " to " + String.join(", ", requestedRoleNames),
                ipAddress,
                userAgent
        );

        return toModel(backofficeUser);
    }

    @Transactional
    public void deleteUser(
            Long userId,
            String actorWalletAddress,
            String actorRole,
            String ipAddress,
            String userAgent
    ) {
        BackofficeUser backofficeUser = backofficeUserDAO.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Backoffice user not found"));

        String username = backofficeUser.getUsername();
        backofficeUserDAO.delete(backofficeUser);

        authAuditTrailService.record(
                "BACKOFFICE_USER_DELETED",
                actorWalletAddress,
                actorRole,
                "BACKOFFICE_USER",
                String.valueOf(userId),
                "Deleted backoffice user " + username,
                ipAddress,
                userAgent
        );
    }

    private BackofficeUserModel toModel(BackofficeUser user) {
        List<String> roles = backofficeAuthorizationService.resolveRoles(user);
        List<String> rights = backofficeAuthorizationService.resolveRights(user);
        return BackofficeUserModel.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .walletAddress(user.getUser().getWalletAddress())
                .role(backofficeAuthorizationService.resolvePrimaryRole(user))
                .roles(roles)
                .rights(rights)
                .build();
    }

    private List<String> resolveRequestedRoleNames(BackofficeUserCreateRequest request) {
        List<String> rawRoles = request.getRoles() != null && !request.getRoles().isEmpty()
                ? request.getRoles()
                : (request.getRole() != null && !request.getRole().isBlank() ? List.of(request.getRole()) : List.of());

        return resolveRequestedRoleNames(rawRoles);
    }

    private List<String> resolveRequestedRoleNames(List<String> rawRoles) {

        if (rawRoles.isEmpty()) {
            throw new BadCredentialsException("At least one role is required");
        }

        return rawRoles.stream()
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .filter(role -> !role.isBlank())
                .distinct()
                .toList();
    }
}
