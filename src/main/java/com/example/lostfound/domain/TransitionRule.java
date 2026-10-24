package com.example.lostfound.domain;

public final class TransitionRule implements LostItemRule {
    @Override
    public void check(LostItemStatus from, LostItemStatus to) {
        LostItemPolicy.move(from, to);
    }
}
