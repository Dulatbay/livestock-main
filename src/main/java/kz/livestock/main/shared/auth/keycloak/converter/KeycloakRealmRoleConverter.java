package kz.livestock.main.shared.auth.keycloak.converter;


import kz.livestock.main.config.AuthConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    public static final String ROLES = "roles";
    private final AuthConfigData authConfigData;


    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList();
        this.addRoles(grantedAuthorities, this.getRealmAccessRoles(jwt));
        this.addRoles(grantedAuthorities, this.getResourceAccessRoles(jwt));
        return new ArrayList(grantedAuthorities);
    }

    private void addRoles(List<SimpleGrantedAuthority> grantedAuthorities, List<String> realmRoles) {
        if (realmRoles != null && !realmRoles.isEmpty()) {
            grantedAuthorities.addAll(realmRoles.stream().map((roleName) -> "ROLE_" + roleName).map(SimpleGrantedAuthority::new).toList());
        }

    }

    private List<String> getResourceAccessRoles(Jwt jwt) {
        List<String> roles = new ArrayList();

        Map<String, Object> resourceAccess = this.getResourceAccessClaim(jwt);
        if (resourceAccess == null) {
            return roles;
        } else {
            this.resolveResourceRealmModuleRoles(roles, resourceAccess);
            return roles;
        }
    }

    private void resolveResourceRealmModuleRoles(List<String> roles, Map<String, Object> resourceAccess) {
        for(String module : this.authConfigData.getResourceRealmModule()) {
            if (resourceAccess.containsKey(module)) {
                Map<String, Object> moduleAccess = (Map)resourceAccess.get(module);
                if (moduleAccess != null && moduleAccess.containsKey(ROLES)) {
                    roles.addAll((List)moduleAccess.get(ROLES));
                }
            }
        }

    }

    private List<String> getRealmAccessRoles(Jwt jwt) {
        Map<String, Object> realmAccess = this.getRealmAccessClaim(jwt);
        return (List<String>)(realmAccess == null ? new ArrayList() : (List)realmAccess.get(ROLES));
    }

    private Map<String, Object> getRealmAccessClaim(Jwt jwt) {
        return (Map)jwt.getClaims().get(REALM_ACCESS_CLAIM);
    }

    private Map<String, Object> getResourceAccessClaim(Jwt jwt) {
        return (Map)jwt.getClaims().get(RESOURCE_ACCESS_CLAIM);
    }

}

