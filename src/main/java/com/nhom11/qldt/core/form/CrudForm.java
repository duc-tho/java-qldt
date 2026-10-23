package com.nhom11.qldt.core.form;

/** Hợp đồng chuyển đổi hai chiều giữa dữ liệu biểu mẫu CRUD và thực thể miền. */
public interface CrudForm<T> {
  /** Tạo thực thể từ dữ liệu hiện có trên biểu mẫu. */
  T toEntity();

  /** Điền dữ liệu của thực thể vào biểu mẫu để hiển thị hoặc chỉnh sửa. */
  void fromEntity(T entity);
}
