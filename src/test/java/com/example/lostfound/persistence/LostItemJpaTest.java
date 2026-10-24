package com.example.lostfound.persistence;

import com.example.lostfound.config.Application;
import com.example.lostfound.config.LostItemService;
import com.example.lostfound.domain.DuplicateLostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = Application.class)
class LostItemJpaTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private LostItemService service;

    @Autowired
    private LostItemRepository repository;

    @BeforeEach
    void emptyTable() {
        jdbc.update("DELETE FROM lost_item");
    }

    @Test
    void secondStatementRollsBack() {
        LostItemKey key = new LostItemKey("LF-ROLLBACK");

        assertThrows(
                DuplicateLostItem.class,
                () -> service.insertTwice(key)
        );

        assertEquals(0, repository.count(key));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        LostItemKey key = new LostItemKey("LF-COMMIT");

        service.register(LostItemId.newId(), key, "Phone");

        assertThrows(
                DuplicateLostItem.class,
                () -> service.register(
                        LostItemId.newId(),
                        key,
                        "Phone again"
                )
        );

        assertEquals(1, repository.count(key));
    }
}
