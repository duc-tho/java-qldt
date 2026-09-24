package com.nhom11.qldt.core.repository;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;

public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final Class<T> domainClass;
    private final EntityManager entityManager;
    protected final FilterQueryBuilder<T> filterQueryBuilder;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.domainClass = entityInformation.getJavaType();
        this.entityManager = entityManager;
        this.filterQueryBuilder = new FilterQueryBuilder<>(domainClass, entityManager);
    }

    @Override
    public Page<T> findAll(
            Pageable pageable,
            String search,
            List<String> searchableFields,
            Map<String, String> filters) {
        return filterQueryBuilder.findAll(pageable, search, searchableFields, filters);
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
