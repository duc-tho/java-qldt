package com.nhom11.qldt.core.service;

public interface BaseService<T, ID> {
    T findById(ID id);

    Iterable<T> findAll();

    T create(T entity);

    T update(T entity);

    void delete(ID id);

    void softDelete(T entity);
}
