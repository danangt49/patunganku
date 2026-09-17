package id.patunganku.settlement_service.domain.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String baseUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
