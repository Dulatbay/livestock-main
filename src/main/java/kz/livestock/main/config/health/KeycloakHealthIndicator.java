package kz.livestock.main.config.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakHealthIndicator implements HealthIndicator {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            // Проверяем конфиг эндпоинт Keycloak
            String url = keycloakUri + "/.well-known/openid-configuration";
            restTemplate.getForObject(url, String.class);
            return Health.up().withDetail("keycloak", "Available").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("keycloak", "Not Available")
                    .withException(e)
                    .build();
        }
    }
}
