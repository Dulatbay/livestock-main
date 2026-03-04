package kz.livestock.main.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class AuthConfigData {
    private String url;
    private String clientId;
    private String realm;
    private List<String> resourceRealmModule;
}
