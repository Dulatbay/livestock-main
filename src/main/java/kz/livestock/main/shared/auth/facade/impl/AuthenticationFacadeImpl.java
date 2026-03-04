package kz.livestock.main.shared.auth.facade.impl;

import kz.livestock.main.shared.auth.facade.IAuthenticationFacade;
import kz.livestock.main.shared.auth.model.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuthenticationFacadeImpl implements IAuthenticationFacade {
    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    @Override
    public List<String> getUserRoles() {
        Authentication authentication = this.getAuthentication();
        return authentication != null ? authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()) : null;
    }

    @Override
    public boolean isUserHasRole(String roleName) {
        Authentication authentication = this.getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch((r) -> Objects.requireNonNull(r.getAuthority()).equalsIgnoreCase("ROLE_" + roleName));
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String getClientId() {
        return this.getPrincipalClaim("azp");
    }

    @Override
    public String resolveOwnerCode(String providedClientId) {
        return "";
    }

    private <T> T getPrincipalClaim(String claimName) {
        T result = null;
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        } else {
            Object principal = this.getAuthentication().getPrincipal();
            if (principal instanceof Jwt jwtPrincipal && jwtPrincipal.hasClaim(claimName)) {
                result = jwtPrincipal.getClaim(claimName);
            }

            return result;
        }
    }

}
