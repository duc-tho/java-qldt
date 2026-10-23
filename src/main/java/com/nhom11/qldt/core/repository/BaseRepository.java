package com.nhom11.qldt.core.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
/** Repository nền tảng bổ sung tìm kiếm, lọc và xóa mềm cho các repository nghiệp vụ. */
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
  /** Truy vấn một trang dữ liệu theo từ khóa trên các trường cho phép và bộ lọc đã xác thực. */
  Page<T> findAll(
      Pageable pageable, String search, List<String> searchableFields, Map<String, String> filters);

  /** Xóa mềm record bằng cách cập nhật {@code deleted_at}, không xóa dữ liệu khỏi database. */
  void softDelete(T entity);

  /** Xóa mềm nhiều record bằng cách cập nhật {@code deleted_at} cho từng record. */
  void softDeleteAll(Iterable<T> entities);

  /** Chạy lệnh DELETE trực tiếp theo ID để xóa cả record đang bị bộ lọc {@code deleted_at} ẩn. */
  boolean deleteByIdIncludingDeleted(ID id);
}
