package com.example.lostfound.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String issue(Authentication authentication) {
        Instant now = Instant.now();
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).toList();
        var claims = JwtClaimsSet.builder().issuer(properties.issuer())
                .subject(authentication.getName()).issuedAt(now).expiresAt(now.plusSeconds(900))
                .claim("roles", roles).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
