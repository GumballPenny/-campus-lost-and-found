package com.example.lostfound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lost_item")
public class LostItemEntity {
    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, unique = true)
    private String businessKey;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LostItemEntity() {
    }

    public LostItemEntity(UUID id, String businessKey, String status, String title) {
        this.id = id;
        this.businessKey = businessKey;
        this.status = status;
        this.title = title;
    }
}
