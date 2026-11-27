package com.example.lostfound.domain;

public record LostItem(
        LostItemId id,
        LostItemKey key,
        LostItemStatus status,
        String title
) {
}
