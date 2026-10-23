package com.nhom11.qldt.core.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import java.io.Serializable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Hiện thực mặc định các thao tác dịch vụ cơ bản bằng repository tương ứng. */
public abstract class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {

  /** Trả về repository của module để các hàm dùng chung có thể đọc và ghi database. */
  public abstract BaseRepository<T, ID> getRepository();

  /** Tìm theo mã và trả về {@code null} khi không có bản ghi. */
  public T findById(ID id) {
    return getRepository().findById(id).orElse(null);
  }

  /** Lấy toàn bộ bản ghi đang khả dụng. */
  public Iterable<T> findAll() {
    return getRepository().findAll();
  }

  /** Lấy dữ liệu theo trang và thứ tự được yêu cầu. */
  public Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  /** Đếm tổng số bản ghi đang khả dụng. */
  public long count() {
    return getRepository().count();
  }

  /** Tạo mới thực thể bằng thao tác lưu của repository. */
  public T create(T entity) {
    return getRepository().save(entity);
  }

  /** Lưu các thay đổi của thực thể hiện có. */
  public T update(T entity) {
    return getRepository().save(entity);
  }

  /** Xóa vĩnh viễn record khỏi database theo ID thông qua repository. */
  public void delete(ID id) {
    getRepository().deleteById(id);
  }

  /** Xóa mềm record bằng cách cập nhật trường {@code deleted_at}, dữ liệu vẫn còn trong database. */
  public void softDelete(T entity) {
    getRepository().softDelete(entity);
  }
}
