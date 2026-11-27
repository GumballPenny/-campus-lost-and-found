package com.example.lostfound.persistence;

import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItem;
import com.example.lostfound.domain.LostItemRepository;
import com.example.lostfound.domain.LostItemStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public Optional<LostItem> findById(LostItemId id) {
        LostItemEntity entity = entityManager.find(LostItemEntity.class, id.value());
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new LostItem(
                new LostItemId(entity.getId()),
                new LostItemKey(entity.getBusinessKey()),
                LostItemStatus.valueOf(entity.getStatus()),
                entity.getTitle()
        ));
    }

    @Override
    public boolean changeStatus(LostItemId id, LostItemStatus from, LostItemStatus to, String actor) {
        int changed = entityManager.createQuery("""
                update LostItemEntity item set item.status = :to
                where item.id = :id and item.status = :from
                """)
                .setParameter("to", to.name()).setParameter("from", from.name())
                .setParameter("id", id.value()).executeUpdate();
        if (changed == 1) {
            entityManager.createNativeQuery("""
                    insert into lost_item_event(item_id, actor, from_status, to_status)
                    values (:id, :actor, :from, :to)
                    """).setParameter("id", id.value()).setParameter("actor", actor)
                    .setParameter("from", from.name()).setParameter("to", to.name()).executeUpdate();
            entityManager.clear();
        }
        return changed == 1;
    }

    @Override
    public java.util.List<LostItem> list() {
        return entityManager.createQuery("select i from LostItemEntity i order by i.createdAt desc", LostItemEntity.class)
                .setMaxResults(100).getResultStream()
                .map(e -> new LostItem(new LostItemId(e.getId()), new LostItemKey(e.getBusinessKey()),
                        LostItemStatus.valueOf(e.getStatus()), e.getTitle())).toList();
    }
}
