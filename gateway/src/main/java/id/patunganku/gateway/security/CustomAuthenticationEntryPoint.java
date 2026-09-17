package id.patunganku.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.patunganku.gateway.model.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /** @noinspection NullableProblems*/
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ApiResponseDto<Void> errorResponse = ApiResponseDto.error("Unauthorized access. Token missing or invalid.");

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(errorResponse))
                .flatMap(bytes -> response.writeWith(Mono.just(response.bufferFactory().wrap(bytes))));
    }
}