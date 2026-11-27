package com.example.lostfound.config;

import com.example.lostfound.domain.DuplicateLostItem;
import com.example.lostfound.domain.LostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemNotFound;
import com.example.lostfound.domain.LostItemRepository;
import com.example.lostfound.domain.LostItemRule;
import com.example.lostfound.domain.LostItemStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.lostfound.domain.InvalidLostItemTransition;
import org.slf4j.LoggerFactory;

@Service
public class LostItemService implements LostItemUseCase {
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
    @Override
    public LostItem register(LostItemId id, LostItemKey key, String title) {
        try {
            repository.insert(id, key, LostItemStatus.REPORTED, title);
            repository.flush();
            return new LostItem(id, key, LostItemStatus.REPORTED, title);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateLostItem(key.value());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public LostItem find(LostItemId id) {
        return repository.findById(id)
                .orElseThrow(() -> new LostItemNotFound(id.value().toString()));
    }

    @Override
    @PreAuthorize("hasRole('CUSTODIAN')")
    @Transactional
    public LostItem markFound(LostItemId id, String actor) {
        LostItem item = repository.findById(id)
                .orElseThrow(() -> new LostItemNotFound(id.value().toString()));
        try {
            rules.check(item.status(), LostItemStatus.FOUND);
        } catch (IllegalStateException exception) {
            throw new InvalidLostItemTransition();
        }
        if (!repository.changeStatus(id, item.status(), LostItemStatus.FOUND, actor)) {
            throw new InvalidLostItemTransition();
        }
        LoggerFactory.getLogger(LostItemService.class).info("Lost item {} marked FOUND by {}", id.value(), actor);
        return new LostItem(item.id(), item.key(), LostItemStatus.FOUND, item.title());
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
