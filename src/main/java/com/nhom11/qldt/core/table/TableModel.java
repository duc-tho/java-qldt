package com.nhom11.qldt.core.table;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public record TableModel<T>(
    Page<T> data,
    String baseUrl,
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

  public List<ColumnDef> searchableColumns() {
    return columns.stream().filter(column -> column.searchable).toList();
  }

  public List<ColumnDef> filterableColumns() {
    return columns.stream().filter(column -> column.filterable).toList();
  }
}
