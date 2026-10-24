package com.example.lostfound.domain;

import java.util.UUID;

public record LostItemId(UUID value) {
    public LostItemId {
        if (value == null) {
            throw new IllegalArgumentException("Lost item ID must not be null");
        }
    }

    public LostItemId(String value) {
        this(parse(value));
    }

    public static LostItemId newId() {
        return new LostItemId(UUID.randomUUID());
    }

    private static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lost item ID must not be null or blank");
        }
        return UUID.fromString(value);
    }
}
