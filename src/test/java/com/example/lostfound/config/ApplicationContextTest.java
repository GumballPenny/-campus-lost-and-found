package com.example.lostfound.config;

import com.example.lostfound.domain.LostItemStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ApplicationContextTest {
    @Autowired
    private LostItemService service;

    @Test
    void springInjectsRulesIntoService() {
        assertEquals(
                LostItemStatus.FOUND,
                service.move(LostItemStatus.REPORTED, LostItemStatus.FOUND)
        );
    }
}
