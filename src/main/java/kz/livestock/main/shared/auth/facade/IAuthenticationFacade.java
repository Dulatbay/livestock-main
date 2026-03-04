package kz.livestock.main.shared.auth.facade;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface IAuthenticationFacade {
    Authentication getAuthentication();

    List<String> getUserRoles();

    boolean isUserHasRole(String roleName);

    boolean isAdmin();

    String getClientId();

    String resolveOwnerCode(String providedClientId);
}
