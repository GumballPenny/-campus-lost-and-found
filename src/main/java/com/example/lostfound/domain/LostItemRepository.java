package com.example.lostfound.domain;

import java.util.Optional;

public interface LostItemRepository {
    void insert(LostItemId id, LostItemKey key, LostItemStatus status, String title);

    void flush();

    int count(LostItemKey key);

    Optional<LostItem> findById(LostItemId id);

    boolean changeStatus(LostItemId id, LostItemStatus from, LostItemStatus to, String actor);

    java.util.List<LostItem> list();
}
