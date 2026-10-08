package com.nhom11.qldt.core.form;

public interface CrudForm<T> {
  T toEntity();

  void fromEntity(T entity);
}
