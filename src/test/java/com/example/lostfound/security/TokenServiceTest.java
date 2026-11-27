package com.example.lostfound.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class TokenServiceTest {
    private final JwtProperties properties=new JwtProperties(
            "test-only-secret-with-at-least-thirty-two-bytes","campus-lost-and-found");
    @Test void signsAndDecodesHs256() {
        var configuration=new JwtConfiguration();
        var service=new TokenService(configuration.jwtEncoder(properties),properties);
        String value=service.issue(new UsernamePasswordAuthenticationToken("finder",null,
                List.of(new SimpleGrantedAuthority("ROLE_FINDER"))));
        var token=configuration.jwtDecoder(properties).decode(value);
        assertEquals("finder",token.getSubject());
        assertEquals("HS256",token.getHeaders().get("alg"));
        assertEquals(List.of("FINDER"),token.getClaimAsStringList("roles"));
        assertEquals("campus-lost-and-found",token.getClaimAsString("iss"));
        assertEquals(900,token.getExpiresAt().getEpochSecond()-token.getIssuedAt().getEpochSecond());
        assertTrue(token.getExpiresAt().isAfter(Instant.now()));
        assertNull(token.getClaim("password"));
    }
    @Test void rejectsWrongIssuerAndShortSecret() {
        var configuration=new JwtConfiguration();
        var token=new TokenService(configuration.jwtEncoder(properties),properties)
                .issue(new UsernamePasswordAuthenticationToken("finder",null,List.of()));
        var other=new JwtProperties(properties.secret(),"other-product");
        assertThrows(org.springframework.security.oauth2.jwt.JwtValidationException.class,
                () -> configuration.jwtDecoder(other).decode(token));
        assertThrows(IllegalArgumentException.class,()->new JwtProperties("short","campus"));
    }
}
