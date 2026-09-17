package id.patunganku.settlement_service.config.client;

import id.patunganku.settlement_service.config.exception.CustomException;
import id.patunganku.settlement_service.domain.model.dto.ExpenseDto;
import id.patunganku.settlement_service.domain.model.dto.GlobalApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventClient {

    private final WebClient webClient;

    public List<ExpenseDto> getExpenses(Long eventId) {

        log.info("[EVENT-CLIENT] Get expenses. eventId={}", eventId);

        if (eventId == null) {
            log.warn("[EVENT-CLIENT] Event ID is null");
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_ID",
                    "Event ID is required"
            );
        }

        String accessToken = getCurrentAccessToken();

        try {
            GlobalApiResponse<List<ExpenseDto>> response = webClient.get()
                    .uri("http://localhost:8081/api/v1/event/{id}/expenses", eventId)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 401, clientResponse -> {
                                log.error("[EVENT-CLIENT] Unauthorized. eventId={}", eventId);
                                return clientResponse.createException();
                            }
                    )
                    .onStatus(
                            status -> status.value() == 403, clientResponse -> {
                                log.error("[EVENT-CLIENT] Forbidden. eventId={}", eventId);
                                return clientResponse.createException();
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError, clientResponse -> {
                                log.error("[EVENT-CLIENT] Failed to get expenses. eventId={}, status={}", eventId, clientResponse.statusCode());
                                return clientResponse.createException();
                            }
                    )
                    .bodyToMono(new ParameterizedTypeReference<GlobalApiResponse<List<ExpenseDto>>>() {})
                    .block();

            if (response == null || response.getData() == null) {
                log.warn("[EVENT-CLIENT] Empty expense response. eventId={}", eventId
                );
                return List.of();
            }

            log.info("[EVENT-CLIENT] Expenses retrieved. eventId={}, count={}", eventId, response.getData().size());

            return response.getData();

        } catch (WebClientResponseException.Unauthorized e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized access to event-service");

        } catch (WebClientResponseException.Forbidden e) {
            throw new CustomException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden access to event-service");

        } catch (WebClientResponseException.NotFound e) {
            throw new CustomException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event or expenses not found");

        } catch (WebClientResponseException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "EVENT_SERVICE_ERROR", "Failed to get expenses from event-service");
        }
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