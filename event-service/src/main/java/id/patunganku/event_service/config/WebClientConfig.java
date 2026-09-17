package id.patunganku.event_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(logRequestMetadata())
                .filter(logResponseMetadata())
                .build();
    }

    private ExchangeFilterFunction logRequestMetadata() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {

            log.debug("[{}] {}", request.method(), request.url());
            request.headers().forEach((name, values) -> {
                for (String value : values) {
                    if (AUTHORIZATION.equalsIgnoreCase(name)) {
                        log.debug("{}: [MASKED]", name);
                    } else {
                        log.debug("{}: {}", name, value);
                    }
                }
            });
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponseMetadata() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("Response Status: {}", response.statusCode());
            response.headers().asHttpHeaders().forEach((name, values) -> {
                for (String value : values) {
                    log.debug("{}: {}", name, value);
                }
            });
            return Mono.just(response);
        });
    }
}
