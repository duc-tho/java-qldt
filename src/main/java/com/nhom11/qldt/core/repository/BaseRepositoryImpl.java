package com.nhom11.qldt.core.repository;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import jakarta.persistence.EntityManager;

public abstract class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
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
}
