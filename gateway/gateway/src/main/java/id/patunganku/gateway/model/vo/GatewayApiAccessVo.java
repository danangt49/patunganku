package id.patunganku.gateway.model.vo;

import java.time.Instant;

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
}
