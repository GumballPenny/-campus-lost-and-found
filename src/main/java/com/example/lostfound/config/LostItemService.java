package com.example.lostfound.config;

import com.example.lostfound.domain.DuplicateLostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemRepository;
import com.example.lostfound.domain.LostItemRule;
import com.example.lostfound.domain.LostItemStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LostItemService {
    private final LostItemRule rules;
    private final LostItemRepository repository;

    public LostItemService(LostItemRule rules, LostItemRepository repository) {
        this.rules = rules;
        this.repository = repository;
    }

    public LostItemStatus move(LostItemStatus from, LostItemStatus to) {
        rules.check(from, to);
        return to;
    }

    @Transactional
    public void register(LostItemId id, LostItemKey key, String title) {
        try {
            repository.insert(id, key, LostItemStatus.REPORTED, title);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateLostItem(key.value());
        }
    }

    @Transactional
    public void insertTwice(LostItemKey key) {
        try {
            repository.insert(LostItemId.newId(), key, LostItemStatus.REPORTED, "First item");
            repository.insert(LostItemId.newId(), key, LostItemStatus.REPORTED, "Duplicate item");
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateLostItem(key.value());
        }
    }
}
