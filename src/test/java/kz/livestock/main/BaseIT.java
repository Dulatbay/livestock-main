package kz.livestock.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.lenient;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Sql(value = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIT {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    protected String tokenWithRoles(String... roles) {
        String token = "test-token-" + UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-keycloak-user-id")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        lenient().when(jwtDecoder.decode(token)).thenReturn(jwt);
        return token;
    }

    protected RequestPostProcessor asAdmin() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + tokenWithRoles("ADMIN"));
            return request;
        };
    }

    protected RequestPostProcessor asOperator() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + tokenWithRoles("OPERATOR"));
            return request;
        };
    }

    protected RequestPostProcessor asUser() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + tokenWithRoles("USER"));
            return request;
        };
    }
}
