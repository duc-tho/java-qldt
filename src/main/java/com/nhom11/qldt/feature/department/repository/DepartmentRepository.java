package com.nhom11.qldt.feature.department.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.department.model.entity.Department;

/** Đọc, ghi và kiểm tra dữ liệu bộ môn trong database. */
public interface DepartmentRepository extends BaseRepository<Department, Long> {
  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByCode(String code);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByCodeAndIdNot(String code, Long id);
}
