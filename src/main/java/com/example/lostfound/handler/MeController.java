package com.example.lostfound.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class MeController {
    public record Me(String username, List<String> authorities) {}
    @Operation(summary = "Current authenticated user")
    @ApiResponse(responseCode = "200", description = "Username and authorities")
    @ApiResponse(responseCode = "401", description = "Valid token required")
    @GetMapping("/api/me")
    public Me me(Authentication auth) {
        return new Me(auth.getName(), auth.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
}
