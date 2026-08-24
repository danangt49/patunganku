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

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private static final String[] SWAGGER_WILDCARD_WHITELIST = {
        "/*/v3/api-docs/**", // Matches: /sales/v3/api-docs/** OR /users/v3/api-docs/**
        "/*/swagger-ui/**",
    };

    @Bean
    public SecurityWebFilterChain publicEndpoints(ServerHttpSecurity http) {
        http
                .securityMatcher(pathMatchers("/sales/verify.html"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll());
        return http.build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                // Whitelisted Url
                .pathMatchers("/").permitAll()
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
