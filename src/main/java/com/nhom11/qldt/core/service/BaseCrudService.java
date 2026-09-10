package com.nhom11.qldt.core.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.nhom11.qldt.core.repository.BaseRepository;

public abstract class BaseCrudService<T, ID extends Serializable> extends BaseServiceImpl<T, ID> {

    public abstract BaseRepository<T, ID> getRepository();

    public abstract Page<T> search(String search, Pageable pageable);

    public Page<T> findAll(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return getRepository().findAll(pageable);
        }
        return search(search.trim(), pageable);
    }

    protected void beforeCreate(T entity) {}

    protected void afterCreate(T entity) {}

    protected void beforeUpdate(T entity) {}

    protected void afterUpdate(T entity) {}

    protected void beforeDelete(T entity) {}

    protected void afterDelete(T entity) {}

    @Override
    @Transactional
    public T create(T entity) {
        beforeCreate(entity);
        T saved = getRepository().save(entity);
        afterCreate(saved);
        return saved;
    }

    @Override
    @Transactional
    public T update(T entity) {
        beforeUpdate(entity);
        T saved = getRepository().save(entity);
        afterUpdate(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(ID id) {
        T entity = getRepository().findById(id).orElse(null);
        if (entity == null) {
            return;
        }

        beforeDelete(entity);
        getRepository().softDelete(entity);
        afterDelete(entity);
    }

    @Transactional
    public void deletePermanently(ID id) {
        T entity = getRepository().findById(id).orElse(null);
        if (entity == null) {
            // already soft-deleted rows are hidden by the entity's SQL restriction
            getRepository().deleteByIdIncludingDeleted(id);

            return;
        }

        beforeDelete(entity);
        getRepository().deleteById(id);
        afterDelete(entity);
    }

    @Transactional
    public void softDeleteAll(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<T> entities = getRepository().findAllById(ids);
        entities.forEach(this::beforeDelete);
        getRepository().softDeleteAll(entities);
        entities.forEach(this::afterDelete);
    }
}
