package com.nhom11.qldt.core.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.TableQuery;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Dịch vụ CRUD nền tảng có hook nghiệp vụ, tìm kiếm bảng và hỗ trợ xóa mềm. */
public abstract class BaseCrudService<T, ID extends Serializable> extends BaseServiceImpl<T, ID> {

  /** Trả về repository của module để lớp base thực hiện các thao tác CRUD. */
  public abstract BaseRepository<T, ID> getRepository();

  /** Lấy dữ liệu phân trang dựa trên cấu hình cột và tham số truy vấn của bảng. */
  public Page<T> findAll(TableQuery query, BaseTableDefinition<T> definition) {
    Pageable pageable = query.pageable(definition);

    // Chỉ truyền các cột được cấu hình tìm kiếm xuống repository để tránh truy vấn tùy ý.
    List<String> searchableFields =
        definition.columns().stream()
            .filter(column -> column.searchable)
            .map(column -> column.field)
            .toList();

    Map<String, String> filters = query.filterValues(definition);

    return getRepository().findAll(pageable, query.getSearch(), searchableFields, filters);
  }

  /** Hook cho phép kiểm tra hoặc chuẩn hóa thực thể trước khi tạo. */
  protected void beforeCreate(T entity) {}

  /** Hook cho phép thực hiện tác vụ bổ sung sau khi tạo thành công. */
  protected void afterCreate(T entity) {}

  /** Hook cho phép kiểm tra hoặc chuẩn hóa thực thể trước khi cập nhật. */
  protected void beforeUpdate(T entity) {}

  /** Hook cho phép thực hiện tác vụ bổ sung sau khi cập nhật thành công. */
  protected void afterUpdate(T entity) {}

  /** Hook cho phép kiểm tra ràng buộc trước khi xóa. */
  protected void beforeDelete(T entity) {}

  /** Hook cho phép thực hiện tác vụ bổ sung sau khi xóa thành công. */
  protected void afterDelete(T entity) {}

  @Override
  @Transactional
  /** Tạo thực thể trong một transaction và chạy các hook trước/sau tương ứng. */
  public T create(T entity) {
    beforeCreate(entity);
    T saved = getRepository().save(entity);
    afterCreate(saved);
    return saved;
  }

  @Override
  @Transactional
  /** Cập nhật thực thể trong một transaction và chạy các hook trước/sau tương ứng. */
  public T update(T entity) {
    beforeUpdate(entity);
    T saved = getRepository().save(entity);
    afterUpdate(saved);
    return saved;
  }

  @Override
  @Transactional
  /** Tìm record theo ID rồi xóa mềm bằng cách cập nhật {@code deleted_at}. */
  public void delete(ID id) {
    T entity = getRepository().findById(id).orElse(null);
    if (entity == null) {
      return;
    }

    beforeDelete(entity);
    getRepository().softDelete(entity);
    afterDelete(entity);
  }

  @Transactional
  /** Xóa vĩnh viễn record khỏi database, kể cả record đã có {@code deleted_at}. */
  public void deletePermanently(ID id) {
    T entity = getRepository().findById(id).orElse(null);
    if (entity == null) {
      // Record có deleted_at bị bộ lọc SQL ẩn đi, vì vậy phải chạy DELETE trực tiếp theo ID.
      getRepository().deleteByIdIncludingDeleted(id);

      return;
    }

    beforeDelete(entity);
    getRepository().deleteById(id);
    afterDelete(entity);
  }

  @Transactional
  /** Xóa mềm nhiều record bằng cách cập nhật {@code deleted_at} và chạy hook cho từng record. */
  public void softDeleteAll(Collection<ID> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    // Nạp trước các bản ghi còn hoạt động để hook chỉ chạy trên dữ liệu thực sự được xóa.
    List<T> entities = getRepository().findAllById(ids);

    entities.forEach(this::beforeDelete);
    getRepository().softDeleteAll(entities);
    entities.forEach(this::afterDelete);
  }
}
