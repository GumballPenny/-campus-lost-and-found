package com.example.lostfound.handler;

import com.example.lostfound.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("dev")
public class AuthController {
    private final AuthenticationManager manager;
    private final TokenService tokens;
    public AuthController(AuthenticationManager manager, TokenService tokens) {
        this.manager = manager; this.tokens = tokens;
    }

    @Operation(summary = "Log in to Campus Lost & Found")
    @ApiResponse(responseCode = "200", description = "Bearer token valid for 900 seconds")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Bad username or password")
    @PostMapping("/api/v1/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = manager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new TokenResponse(tokens.issue(authentication), "Bearer", 900);
    }
}
