package com.example.lostfound.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReturnedItemIsFinalRuleTest {
    private final ReturnedItemIsFinalRule rule = new ReturnedItemIsFinalRule();

    @Test
    void allowsCheckingAnActiveItem() {
        assertDoesNotThrow(() -> rule.check(LostItemStatus.REPORTED, LostItemStatus.FOUND));
    }

    @Test
    void stopsMovementAfterReturn() {
        assertThrows(
                IllegalStateException.class,
                () -> rule.check(LostItemStatus.RETURNED, LostItemStatus.FOUND)
        );
    }
}
