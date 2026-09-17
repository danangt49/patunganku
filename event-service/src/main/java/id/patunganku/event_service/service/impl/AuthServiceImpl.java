package id.patunganku.event_service.service.impl;

import id.patunganku.event_service.config.exception.CustomException;
import id.patunganku.event_service.domain.model.dto.GetTokenDto;
import id.patunganku.event_service.domain.model.properties.KeycloakProperties;
import id.patunganku.event_service.domain.model.vo.LoginVo;
import id.patunganku.event_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WebClient webClient;
    private final KeycloakProperties properties;

    @Override
    public GetTokenDto login(LoginVo vo) {
        log.info("[LOGIN] Start login process for user: {}", vo.getUsernameOrEmail());

        return webClient.post()
                .uri(
                        properties.getBaseUrl()
                                + "/realms/{realm}/protocol/openid-connect/token",
                        properties.getRealm()
                )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        BodyInserters.fromFormData(
                                        "grant_type",
                                        "password"
                                )
                                .with("client_id", properties.getClientId())
                                .with("client_secret", properties.getClientSecret())
                                .with("username", vo.getUsernameOrEmail())
                                .with("password", vo.getPassword())
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[LOGIN] Keycloak 4xx response: {}", body);

                                    return Mono.error(
                                            new CustomException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", body)
                                    );
                                })
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[LOGIN] Keycloak 5xx response: {}", body);

                                    return Mono.error(
                                            new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "KEYCLOAK_SERVER_ERROR", body)
                                    );
                                })
                )
                .bodyToMono(GetTokenDto.class)
                .block();
    }
}