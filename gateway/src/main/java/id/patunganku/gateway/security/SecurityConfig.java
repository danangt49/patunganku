package id.patunganku.gateway.security;

import java.util.Collection;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private static final String[] SWAGGER_WILDCARD_WHITELIST = {
        "/",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs",
        "/event/swagger-ui/**",
        "/event/swagger-ui.html",
        "/event/v3/api-docs/**",
        "/event/v3/api-docs",
        "/event/api/v1/auth/login",
        "/settlement/swagger-ui/**",
        "/settlement/swagger-ui.html",
        "/settlement/v3/api-docs/**",
        "/settlement/v3/api-docs",
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                // Whitelisted Url
                .pathMatchers(SWAGGER_WILDCARD_WHITELIST).permitAll()

                .anyExchange().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();

    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {

      JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
      roles.setAuthoritiesClaimName("realm_access.roles");
      roles.setAuthorityPrefix("ROLE_");
      ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();

      // Wrap the non-reactive converter into a Flux
      converter.setJwtGrantedAuthoritiesConverter(jwt -> {
          Collection<GrantedAuthority> authorities = roles.convert(jwt);
          return Flux.fromIterable(authorities);
      });

      return converter;
  }
}
