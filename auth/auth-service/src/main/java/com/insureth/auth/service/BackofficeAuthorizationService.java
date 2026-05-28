package com.insureth.auth.service;

import com.insureth.auth.domain.entity.BackofficeRole;
import com.insureth.auth.domain.entity.BackofficeUser;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BackofficeAuthorizationService {

    private static final List<String> ROLE_PRIORITY = List.of(
            "SUPER_ADMIN",
            "USER_ADMIN",
            "GOVERNANCE_MANAGER",
            "OPERATOR",
            "REVIEWER"
    );

    public List<String> resolveRoles(BackofficeUser user) {
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .filter(role -> role != null && !role.isBlank())
                .distinct()
                .sorted(this::compareRoles)
                .toList();
    }

    public List<String> resolveRights(BackofficeUser user) {
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole())
                .map(BackofficeRole::getRoleRights)
                .flatMap(Set::stream)
                .map(roleRight -> roleRight.getRightCode())
                .filter(right -> right != null && !right.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public String resolvePrimaryRole(BackofficeUser user) {
        return resolveRoles(user).stream().findFirst().orElse("BACKOFFICE_USER");
    }

    public List<String> buildAuthorities(BackofficeUser user) {
        Set<String> authorities = new LinkedHashSet<>();
        resolveRoles(user).forEach(role -> authorities.add("ROLE_" + role));
        authorities.addAll(resolveRights(user));
        return List.copyOf(authorities);
    }

    public String resolvePrimaryRoleFromAuthorities(List<String> authorities) {
        return authorities.stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .min(this::compareRoles)
                .orElse("BACKOFFICE_USER");
    }

    private int compareRoles(String left, String right) {
        return Comparator
                .comparingInt((String role) -> {
                    int index = ROLE_PRIORITY.indexOf(role);
                    return index >= 0 ? index : Integer.MAX_VALUE;
                })
                .thenComparing(String::compareTo)
                .compare(left, right);
    }
}
