package id.patunganku.gateway.security.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import id.patunganku.gateway.model.vo.GatewayApiAccessVo;
import id.patunganku.gateway.util.KeycloakTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** @noinspection NullableProblems*/
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAccessApiFilter implements GlobalFilter, Ordered {

    private final KeycloakTokenUtil keycloakTokenUtil;

    @Value("${webclient.report.url}")
    private String reportUrl;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString();
        log.info("[REQUEST] Generated trace id = {}", traceId);
        exchange.getRequest().mutate()
              .header("X-TRACE-ID", traceId)
              .build();

        return chain.filter(exchange)
                .doFinally(_ -> sendLog(exchange, start, traceId));
    }

    private void sendLog(ServerWebExchange exchange, long start, String traceId) {

        final String GATEWAY_SOURCE = "SERVICES";
        long latency = System.currentTimeMillis() - start;
        var servicePath = exchange.getRequest().getPath();

        GatewayApiAccessVo event = GatewayApiAccessVo.builder()
                .requestPath(servicePath.value())
                .serviceName(getServiceName(exchange))
                .source(GATEWAY_SOURCE)
                .queryString(exchange.getRequest().getQueryParams().toSingleValueMap()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString())
                .traceId(traceId)
                .httpMethod(exchange.getRequest().getMethod().name())
                .requestPath(exchange.getRequest().getURI().getPath())
                .httpStatus(exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0)
                .latencyMs(latency)
                .clientIp(getClientIp(exchange))
                .clientId(keycloakTokenUtil.extractClientId(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst("User-Agent"))
                .requestTimestamp(Instant.now())
                .routeId(getRouteId(exchange))
                .build();

        client().post()
                .uri("/api/v1/gateway-api-access")
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofMillis(300))
                .onErrorResume(e -> {
                    log.error("Error Saving Gateway Api Access {}", e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null) return xff.split(",")[0];
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private WebClient client() {
        WebClient.builder().build();
        return WebClient.builder().baseUrl(reportUrl).build();
    }

    private String getRouteId(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getId() : null;
    }

    private String getServiceName(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getUri().getHost() : null;
    }
}
