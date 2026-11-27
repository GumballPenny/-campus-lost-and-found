package com.example.lostfound.config;

import com.example.lostfound.domain.LostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;

public interface LostItemUseCase {
    LostItem register(LostItemId id, LostItemKey key, String title);

    LostItem find(LostItemId id);

    LostItem markFound(LostItemId id, String actor);
}
