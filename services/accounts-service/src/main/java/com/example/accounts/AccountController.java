package com.example.accounts;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/internal/auth")
public class AccountController {
    public record Login(@NotBlank String username, @NotBlank String password) {}
    public record Register(@NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,40}") String username,
                           @NotBlank @Size(min=8,max=72) String password,
                           @NotBlank @Size(max=80) String displayName, String invitation) {}
    public record Token(String accessToken,String tokenType,long expiresIn) {}
    private final AccountService service;
    public AccountController(AccountService service) { this.service=service; }
    @PostMapping("/login") public Token login(@Valid @RequestBody Login request) { return service.login(request); }
    @PostMapping("/register") public ResponseEntity<Token> register(@Valid @RequestBody Register request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }
}
