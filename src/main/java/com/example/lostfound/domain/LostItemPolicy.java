package com.example.lostfound.domain;

public final class LostItemPolicy {
    private LostItemPolicy() {
    }

    public static LostItemStatus move(LostItemStatus from, LostItemStatus to) {
        boolean allowed = from == LostItemStatus.REPORTED && to == LostItemStatus.FOUND
                || from == LostItemStatus.FOUND && to == LostItemStatus.RETURNED;
        if (!allowed) {
            throw new IllegalStateException("Transition from " + from + " to " + to + " is forbidden");
        }
        return to;
    }
}
