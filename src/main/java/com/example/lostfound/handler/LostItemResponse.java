package com.example.lostfound.handler;

import com.example.lostfound.domain.LostItem;

import java.util.UUID;

public record LostItemResponse(
        UUID id,
        String businessKey,
        String status,
        String title
) {
    public static LostItemResponse from(LostItem item) {
        return new LostItemResponse(
                item.id().value(),
                item.key().value(),
                item.status().name(),
                item.title()
        );
    }
}
