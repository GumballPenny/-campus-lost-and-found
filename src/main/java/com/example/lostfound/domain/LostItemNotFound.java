package com.example.lostfound.domain;

public class LostItemNotFound extends RuntimeException {
    public LostItemNotFound(String id) {
        super("Lost item not found: " + id);
    }
}
