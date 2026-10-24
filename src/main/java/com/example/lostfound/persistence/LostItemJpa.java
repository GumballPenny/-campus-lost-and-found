package com.example.lostfound.persistence;

import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemRepository;
import com.example.lostfound.domain.LostItemStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class LostItemJpa implements LostItemRepository {
    private final EntityManager entityManager;

    public LostItemJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void insert(LostItemId id, LostItemKey key, LostItemStatus status, String title) {
        LostItemEntity entity = new LostItemEntity(
                id.value(),
                key.value(),
                status.name(),
                title
        );
        entityManager.persist(entity);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public int count(LostItemKey key) {
        Long result = entityManager.createQuery("""
                select count(item)
                from LostItemEntity item
                where item.businessKey = :key
                """, Long.class)
                .setParameter("key", key.value())
                .getSingleResult();
        return result.intValue();
    }
}
