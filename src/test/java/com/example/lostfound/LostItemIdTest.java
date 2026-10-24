package com.example.lostfound;

import com.example.lostfound.domain.LostItemId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

class LostItemIdTest {
    @Test
    void createsIdWithValidValue() {
        UUID value = UUID.randomUUID();
        LostItemId id = new LostItemId(value);
        assertEquals(value, id.value());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new LostItemId((UUID) null));
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new LostItemId("   "));
    }

    @Test
    void createsDifferentNewIds() {
        LostItemId first = LostItemId.newId();
        LostItemId second = LostItemId.newId();
        assertNotNull(first.value());
        assertNotEquals(first, second);
    }
}
