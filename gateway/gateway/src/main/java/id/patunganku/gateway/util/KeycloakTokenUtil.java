package id.patunganku.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Base64;
import java.util.Map;

@Component
public class KeycloakTokenUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /** @noinspection rawtypes, unchecked */
    public String extractClientId(ServerWebExchange exchange) {

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            Map payload = mapper.readValue(payloadJson, Map.class);

            // Priority order
            if (payload.containsKey("azp")) {
                return payload.get("azp").toString();
            }

            if (payload.containsKey("clientId")) {
                return payload.get("clientId").toString();
            }

            if (payload.containsKey("resource_access")) {
                Map<String, Object> ra = (Map<String, Object>) payload.get("resource_access");

                if (!ra.isEmpty()) {
                    return ra.keySet().iterator().next();
                }
            }

        } catch (Exception _) {
            // ignore: logging only
        }

        return null;
    }
}

