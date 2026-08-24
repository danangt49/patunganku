package id.patunganku.event_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(servers = {@Server(url = "/event", description = "PatunganKu Event Service")})
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PatunganKu Event Service API")
                        .version("v1.0.0")
                        .description("API documentation for PatunganKu Event Service")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                )
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME)
                );
    }

    @Bean
    public GroupedOpenApi eventApiV1() {
        return GroupedOpenApi.builder()
                .group("API - V1")
                .pathsToMatch("/api/**")
                .build();
    }
}