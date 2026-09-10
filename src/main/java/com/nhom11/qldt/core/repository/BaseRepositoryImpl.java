package com.nhom11.qldt.core.repository;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;

public abstract class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final Class<T> domainClass;
    private final EntityManager entityManager;

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);

        this.domainClass = domainClass;
        this.entityManager = entityManager;
    }

    @Override
    public void softDelete(T entity) {
        if (!(entity instanceof SoftDeleteEntity)) {
            throw new IllegalArgumentException("Entity does not support soft delete");
        }

        SoftDeleteEntity softDeleteEntity = (SoftDeleteEntity) entity;
        softDeleteEntity.setDeletedAt(Instant.now());

        save(entity);
    }

    @Override
    public void softDeleteAll(Iterable<T> entities) {
        for (T entity : entities) {
            if (!(entity instanceof SoftDeleteEntity)) {
                throw new IllegalArgumentException("Entity does not support soft delete");
            }
            ((SoftDeleteEntity) entity).setDeletedAt(Instant.now());
        }

        saveAll(entities);
    }

    @Override
    public boolean deleteByIdIncludingDeleted(ID id) {
        return entityManager.createNativeQuery("DELETE FROM " + resolveTableName() + " WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate() > 0;
    }

    private String resolveTableName() {
        Table table = domainClass.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }

        Entity entity = domainClass.getAnnotation(Entity.class);

        return entity != null && !entity.name().isBlank() ? entity.name() : domainClass.getSimpleName();
    }
}
