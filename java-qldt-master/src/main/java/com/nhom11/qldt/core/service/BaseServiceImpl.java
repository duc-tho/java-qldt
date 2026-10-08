package com.nhom11.qldt.core.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import java.io.Serializable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {

  public abstract BaseRepository<T, ID> getRepository();

  public T findById(ID id) {
    return getRepository().findById(id).orElse(null);
  }

  public Iterable<T> findAll() {
    return getRepository().findAll();
  }

  public Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  public T create(T entity) {
    return getRepository().save(entity);
  }

  public T update(T entity) {
    return getRepository().save(entity);
  }

  public void delete(ID id) {
    getRepository().deleteById(id);
  }

  public void softDelete(T entity) {
    getRepository().softDelete(entity);
  }
}
