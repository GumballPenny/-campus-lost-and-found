package com.example.lostfound.domain;

public interface LostItemRepository {
    void insert(LostItemId id, LostItemKey key, LostItemStatus status, String title);

    void flush();

    int count(LostItemKey key);
}
