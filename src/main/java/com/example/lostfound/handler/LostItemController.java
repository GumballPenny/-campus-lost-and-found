package com.example.lostfound.handler;

import com.example.lostfound.config.LostItemUseCase;
import com.example.lostfound.domain.LostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lost-items")
public class LostItemController {
    private final LostItemUseCase service;

    public LostItemController(LostItemUseCase service) {
        this.service = service;
    }

    @Operation(summary = "Register a lost item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Business key already exists")
    })
    @PostMapping
    public ResponseEntity<LostItemResponse> create(@Valid @RequestBody CreateLostItemRequest request) {
        LostItem item = service.register(
                LostItemId.newId(),
                new LostItemKey(request.businessKey()),
                request.title()
        );
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(item.id().value())
                .toUri();
        return ResponseEntity.created(location).body(LostItemResponse.from(item));
    }

    @Operation(summary = "Read a lost item by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public LostItemResponse get(@PathVariable UUID id) {
        return LostItemResponse.from(service.find(new LostItemId(id)));
    }

    @Operation(summary = "Custodian marks a reported item as found")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed to FOUND"),
            @ApiResponse(responseCode = "401", description = "Valid token required"),
            @ApiResponse(responseCode = "403", description = "Custodian role required"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "409", description = "Transition forbidden")
    })
    @PostMapping("/{id}/found")
    public LostItemResponse markFound(@PathVariable UUID id,
                                      org.springframework.security.core.Authentication authentication) {
        return LostItemResponse.from(service.markFound(new LostItemId(id), authentication.getName()));
    }
}
