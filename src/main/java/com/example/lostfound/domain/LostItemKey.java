package com.example.lostfound.domain;

public record LostItemKey(String value) {
    public LostItemKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lost item key must not be null or blank");
        }
    }
}
