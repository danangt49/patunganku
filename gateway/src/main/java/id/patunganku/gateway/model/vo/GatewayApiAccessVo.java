package id.patunganku.gateway.model.vo;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayApiAccessVo {

    private String traceId;
    private String routeId;
    private String source;
    private String serviceName;
    private String httpMethod;
    private Integer httpStatus;
    private String requestPath;
    private String queryString;
    private Long latencyMs;
    private String clientId;
    private String clientIp;
    private String userAgent;
    private String decision;
    private Instant requestTimestamp;

    public DiscordWebhook toDiscord() {
        return DiscordWebhook.builder()
                .content("Gateway API Access")
                .embeds(Collections.singletonList(
                        DiscordEmbed.builder()
                                .title("🌐 Gateway API Access")
                                .description("API request melalui Gateway")
                                .color(httpStatus != null && httpStatus >= 400 ? 15158332 : 3066993)
                                .timestamp(requestTimestamp != null ? requestTimestamp.toString() : Instant.now().toString())
                                .fields(Arrays.asList(
                                        field("Service", serviceName, true),
                                        field("Route", routeId, true),
                                        field("Method", httpMethod, true),
                                        field("Status", String.valueOf(httpStatus), true),
                                        field("Latency", latencyMs + " ms", true),
                                        field("Client IP", clientIp, true),
                                        field("Client ID", clientId, true),
                                        field("Decision", decision, true),
                                        field("Trace ID", traceId, false),
                                        field("Path", requestPath, false),
                                        field("Query String", queryString, false),
                                        field("User Agent", userAgent, false)
                                ))
                                .build()
                ))
                .build();
    }

    private DiscordField field(String name, String value, boolean inline) {
        return DiscordField.builder()
                .name(name)
                .value(value != null ? value : "-")
                .inline(inline)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscordWebhook {
        private String content;
        private java.util.List<DiscordEmbed> embeds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscordEmbed {
        private String title;
        private String description;
        private Integer color;
        private String timestamp;
        private java.util.List<DiscordField> fields;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscordField {
        private String name;
        private String value;
        private Boolean inline;
    }
}