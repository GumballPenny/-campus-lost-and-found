package com.example.lostfound.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, SecurityErrors errors, Environment env) throws Exception {
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);

        http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable()).httpBasic(b -> b.disable())
                .authorizeHttpRequests(a -> {
                    a.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                    if (env.acceptsProfiles(Profiles.of("final"))) {
                        a.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll();
                    }
                    a.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                            .requestMatchers("/", "/index.html", "/app.css", "/app.js", "/favicon.ico").permitAll()
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/lost-items/*/found").hasRole("CUSTODIAN")
                            .requestMatchers(HttpMethod.GET, "/api/me").hasAnyRole("FINDER", "CUSTODIAN")
                            .requestMatchers("/api/v1/**").hasAnyRole("FINDER", "CUSTODIAN")
                            .anyRequest().denyAll();
                })
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((r, s, x) -> errors.write(r,s,401,"UNAUTHENTICATED","valid token required"))
                        .accessDeniedHandler((r,s,x) -> errors.write(r,s,403,"FORBIDDEN","not allowed for your role")))
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint((r,s,x) -> errors.write(r,s,401,"UNAUTHENTICATED","valid token required"))
                        .accessDeniedHandler((r,s,x) -> errors.write(r,s,403,"FORBIDDEN","not allowed for your role")));
        return http.build();
    }
}
