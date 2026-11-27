package com.example.accounts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Service
public class AccountService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final AuthenticationManager manager;
    private final JwtEncoder encoder;
    private final String issuer;
    private final String invitation;

    public AccountService(JdbcTemplate jdbc, PasswordEncoder passwords, AuthenticationManager manager,
                          JwtEncoder encoder, @Value("${app.issuer}") String issuer,
                          @Value("${app.custodian-invite}") String invitation) {
        if (invitation.length() < 16) throw new IllegalArgumentException("Custodian invite must contain at least 16 characters");
        this.jdbc=jdbc; this.passwords=passwords; this.manager=manager; this.encoder=encoder;
        this.issuer=issuer; this.invitation=invitation;
    }
    @Transactional
    public AccountController.Token register(AccountController.Register request) {
        String role="FINDER";
        if (request.invitation()!=null && !request.invitation().isBlank()) {
            if (!MessageDigest.isEqual(invitation.getBytes(StandardCharsets.UTF_8),
                    request.invitation().getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("invalid invitation");
            }
            role="CUSTODIAN";
        }
        UUID id=UUID.randomUUID();
        jdbc.update("insert into users(id,username,display_name,password_hash,role) values (?,?,?,?,?)",
                id,request.username(),request.displayName(),passwords.encode(request.password()),role);
        jdbc.update("insert into auth_event(user_id,action) values (?,?)",id,"REGISTER");
        return token(new UsernamePasswordAuthenticationToken(request.username(),null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+role))));
    }
    @Transactional
    public AccountController.Token login(AccountController.Login request) {
        Authentication auth=manager.authenticate(new UsernamePasswordAuthenticationToken(request.username(),request.password()));
        jdbc.update("insert into auth_event(user_id,action) select id,? from users where username=?","LOGIN",auth.getName());
        return token(auth);
    }
    private AccountController.Token token(Authentication auth) {
        Instant now=Instant.now();
        var roles=auth.getAuthorities().stream().map(a -> a.getAuthority().replaceFirst("^ROLE_","")).toList();
        var claims=JwtClaimsSet.builder().issuer(issuer).subject(auth.getName())
                .issuedAt(now).expiresAt(now.plusSeconds(900)).claim("roles",roles).build();
        String jwt=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new AccountController.Token(jwt,"Bearer",900);
    }
}
