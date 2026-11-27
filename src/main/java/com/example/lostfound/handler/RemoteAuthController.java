package com.example.lostfound.handler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import java.util.UUID;

@RestController
@Profile("final")
public class RemoteAuthController {
    private final RestClient accounts;
    public RemoteAuthController(RestClient accountsRestClient) { this.accounts = accountsRestClient; }
    public record RegisterRequest(
            @NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,40}") String username,
            @NotBlank @Size(min=8,max=72) String password,
            @NotBlank @Size(max=80) String displayName,
            String invitation) {}

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return forward("/internal/auth/login", request, http);
    }
    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return forward("/internal/auth/register", request, http);
    }
    private ResponseEntity<?> forward(String path, Object body, HttpServletRequest request) {
        String correlation = request.getHeader("X-Correlation-Id");
        if (correlation == null || correlation.isBlank()) correlation = UUID.randomUUID().toString();
        try {
            return accounts.post().uri(path).header("X-Correlation-Id",correlation)
                    .body(body).retrieve().toEntity(TokenResponse.class);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            String code = status == 401 ? "UNAUTHENTICATED" : status == 409 ? "DUPLICATE" : "VALIDATION";
            String message = status == 401 ? "bad username or password" :
                    status == 409 ? "username already exists" : "registration details are invalid";
            if (status != 400 && status != 401 && status != 409) {
                status = 503; code = "UNAVAILABLE"; message = "account service temporarily unavailable";
            }
            return ResponseEntity.status(status).body(new ApiError(code,message,correlation));
        } catch (ResourceAccessException exception) {
            return ResponseEntity.status(503).body(new ApiError("UNAVAILABLE",
                    "account service temporarily unavailable",correlation));
        }
    }
}
