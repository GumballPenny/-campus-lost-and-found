package com.example.lostfound.domain;

public interface LostItemRule {
    void check(LostItemStatus from, LostItemStatus to);
}
