package com.nhom11.qldt.core.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    Page<T> findAll(
            Pageable pageable,
            String search,
            List<String> searchableFields,
            Map<String, String> filters);

    void softDelete(T entity);

    void softDeleteAll(Iterable<T> entities);

    boolean deleteByIdIncludingDeleted(ID id);
}
