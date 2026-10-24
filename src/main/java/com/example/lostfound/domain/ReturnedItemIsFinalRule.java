package com.example.lostfound.domain;

public final class ReturnedItemIsFinalRule implements LostItemRule {
    @Override
    public void check(LostItemStatus from, LostItemStatus to) {
        if (from == LostItemStatus.RETURNED) {
            throw new IllegalStateException("A returned item has completed its lifecycle");
        }
    }
}
