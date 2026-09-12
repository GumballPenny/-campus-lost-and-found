package com.example.lostfound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LostItemIdTest {
    @Test
    void createsIdWithValidValue() {
        LostItemId id = new LostItemId("ITEM-1");
        assertEquals("ITEM-1", id.value());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new LostItemId(null));
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new LostItemId("   "));
    }
}
