package com.example.lostfound;

public record LostItemId(String value) {
    public LostItemId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lost item ID must not be null or blank");
        }
    }
}
