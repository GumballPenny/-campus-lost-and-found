package com.example.lostfound.config;

import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemRepository;
import com.example.lostfound.domain.LostItemRule;
import com.example.lostfound.domain.LostItemStatus;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LostItemServiceTest {
    @Test
    void registerUsesMockedOutboundPort() {
        LostItemRule rules = mock(LostItemRule.class);
        LostItemRepository repository = mock(LostItemRepository.class);
        LostItemService service = new LostItemService(rules, repository);
        LostItemId id = LostItemId.newId();
        LostItemKey key = new LostItemKey("LF-1");

        service.register(id, key, "Phone");

        verify(repository).insert(id, key, LostItemStatus.REPORTED, "Phone");
        verify(repository).flush();
    }
}
