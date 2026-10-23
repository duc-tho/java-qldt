package com.nhom11.qldt.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Hợp đồng các thao tác dữ liệu cơ bản dùng chung cho tầng dịch vụ. */
public interface BaseService<T, ID> {
  /** Tìm thực thể theo mã định danh. */
  T findById(ID id);

  /** Lấy toàn bộ thực thể đang khả dụng. */
  Iterable<T> findAll();

  /** Lấy một trang thực thể theo cấu hình phân trang. */
  Page<T> findAll(Pageable pageable);

  /** Đếm tổng số thực thể đang khả dụng. */
  long count();

  /** Tạo mới một thực thể. */
  T create(T entity);

  /** Lưu các thay đổi của một thực thể. */
  T update(T entity);

  /** Xóa vĩnh viễn record khỏi database theo ID. */
  void delete(ID id);

  /** Xóa mềm record bằng cách cập nhật trường {@code deleted_at}. */
  void softDelete(T entity);
}
