package com.example.lostfound;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LostItemPolicyTest {
    @ParameterizedTest
    @CsvSource({
            "REPORTED, FOUND",
            "FOUND, RETURNED"
    })
    void allowsValidTransitions(LostItemStatus from, LostItemStatus to) {
        assertEquals(to, LostItemPolicy.move(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "REPORTED, RETURNED",
            "RETURNED, FOUND"
    })
    void rejectsForbiddenTransitions(LostItemStatus from, LostItemStatus to) {
        assertThrows(IllegalStateException.class, () -> LostItemPolicy.move(from, to));
    }
}
