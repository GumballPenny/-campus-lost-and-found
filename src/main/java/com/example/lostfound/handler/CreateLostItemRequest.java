package com.example.lostfound.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLostItemRequest(
        @NotBlank String businessKey,
        @NotBlank @Size(max = 120) String title
) {
}
