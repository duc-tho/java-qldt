package com.nhom11.qldt.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseService<T, ID> {
  T findById(ID id);

  Iterable<T> findAll();

  Page<T> findAll(Pageable pageable);

  long count();

  T create(T entity);

  T update(T entity);

  void delete(ID id);

  void softDelete(T entity);
}
