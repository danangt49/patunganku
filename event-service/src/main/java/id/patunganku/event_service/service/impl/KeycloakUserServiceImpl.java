package id.patunganku.event_service.service.impl;

import id.patunganku.event_service.config.exception.CustomException;
import id.patunganku.event_service.domain.model.dto.KeycloakUserDto;
import id.patunganku.event_service.domain.model.properties.KeycloakProperties;
import id.patunganku.event_service.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final WebClient webClient;
    private final KeycloakProperties properties;

    @Override
    public KeycloakUserDto getUser(String userId) {
        log.info("[KEYCLOAK-USER] Get user. userId={}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("[KEYCLOAK-USER] User ID is empty");
            throw new CustomException(HttpStatus.BAD_REQUEST, "INVALID_USER_ID", "User ID is required");
        }

        String accessToken = getCurrentAccessToken();

        try {
            KeycloakUserDto user = webClient.get()
                    .uri(properties.getBaseUrl() + "/admin/realms/{realm}/users/{userId}", properties.getRealm(), userId)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            response -> {
                                log.warn("[KEYCLOAK-USER] User not found. userId={}", userId);
                                return response.createException();
                            }
                    )
                    .onStatus(
                            status -> status.value() == 401,
                            response -> {
                                log.error("[KEYCLOAK-USER] Unauthorized when accessing Keycloak. userId={}", userId);
                                return response.createException();
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> {
                                log.error("[KEYCLOAK-USER] Failed to get user. userId={}, status={}", userId, response.statusCode());
                                return response.createException();
                            }
                    )
                    .bodyToMono(KeycloakUserDto.class)
                    .block();

            if (user == null) {
                log.warn("[KEYCLOAK-USER] User response is empty. userId={}", userId);
                throw new CustomException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Keycloak user not found");
            }

            log.info("[KEYCLOAK-USER] User found. userId={}, username={}, name={}", user.getId(), user.getUsername(), user.getName());

            return user;

        } catch (WebClientResponseException.NotFound e) {
            throw new CustomException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Keycloak user not found");

        } catch (WebClientResponseException.Unauthorized e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized access to Keycloak");

        } catch (WebClientResponseException.Forbidden e) {
            throw new CustomException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden access to Keycloak");

        } catch (WebClientResponseException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR", "Failed to get user from Keycloak");
        }
    }

    @Override
    public void validateUser(String userId) {
        log.debug("[KEYCLOAK-USER] Validate user. userId={}", userId);

        getUser(userId);

        log.debug("[KEYCLOAK-USER] User valid. userId={}", userId);
    }

    @Override
    public String getUserName(String userId) {
        return getUser(userId).getName();
    }

    private String getCurrentAccessToken() {
        log.debug("[KEYCLOAK-TOKEN] Getting access token from current authentication");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String accessToken = jwtAuthenticationToken.getToken().getTokenValue();
            if (!accessToken.trim().isEmpty()) {
                log.debug("[KEYCLOAK-TOKEN] Access token found. userId={}", jwtAuthenticationToken.getToken().getSubject());
                return accessToken;
            }
        }

        log.warn("[KEYCLOAK-TOKEN] Access token not found");
        throw new CustomException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Access token not found");
    }
}