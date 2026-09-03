package com.nhom11.qldt.core.service;

import java.io.Serializable;
import com.nhom11.qldt.core.repository.BaseRepository;

public abstract class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {

    public abstract BaseRepository<T, ID> getRepository();

    public T findById(ID id) {
        // Implementation for finding entity by ID
        return null;
    }

    public Iterable<T> findAll() {
        // Implementation for finding all entities
        return null;
    }

    public T create(T entity) {
        // Implementation for creating entity
        return null;
    }

    public T update(T entity) {
        // Implementation for updating entity
        return null;
    }

    public void delete(ID id) {
        // Implementation for deleting entity
    }

    public void softDelete(T entity) {
        // Implementation for soft deleting entity
    }
}
