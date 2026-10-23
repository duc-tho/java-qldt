package com.nhom11.qldt.core.table;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

/** Gom toàn bộ dữ liệu và cấu hình cần thiết để Thymeleaf dựng bảng CRUD. */
public record TableModel<T>(
    Page<T> data,
    String baseUrl,
    String createUrl,
    String bulkDeleteUrl,
    String search,
    String sortField,
    String sortDir,
    int pageSize,
    Map<String, String> filters,
    List<Integer> pageSizeOptions,
    List<ColumnDef> columns,
    List<ActionDef> actions,
    boolean bulkDeleteEnabled,
    boolean canCreate) {

  /** Lọc ra các cột tham gia tìm kiếm tự do. */
  public List<ColumnDef> searchableColumns() {
    return columns.stream().filter(column -> column.searchable).toList();
  }

  /** Lọc ra các cột có bộ lọc riêng trên giao diện. */
  public List<ColumnDef> filterableColumns() {
    return columns.stream().filter(column -> column.filterable).toList();
  }
}
