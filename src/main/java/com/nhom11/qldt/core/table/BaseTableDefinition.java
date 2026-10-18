package com.nhom11.qldt.core.table;

import java.util.List;
import java.util.Optional;

public abstract class BaseTableDefinition<T> {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final String DEFAULT_SORT = "";
  private static final String DEFAULT_SORT_DIRECTION = "asc";
  private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(10, 20, 50);

  public abstract List<ColumnDef> columns();

  public abstract List<ActionDef> actions(String baseUrl);

  public boolean bulkDeleteEnabled() {
    return false;
  }

  public boolean canCreate() {
    return true;
  }

  public String createUrl(String baseUrl) {
    return baseUrl + "/new";
  }

  public String bulkDeleteUrl(String baseUrl) {
    return baseUrl + "/delete";
  }

  public int defaultPage() {
    return DEFAULT_PAGE;
  }

  public int defaultPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  public String defaultSort() {
    return DEFAULT_SORT;
  }

  public String defaultSortDirection() {
    return DEFAULT_SORT_DIRECTION;
  }

  public List<Integer> pageSizeOptions() {
    return PAGE_SIZE_OPTIONS;
  }

  public boolean isSearchable(String field) {
    return columns().stream().anyMatch(column -> column.field.equals(field) && column.searchable);
  }

  public Optional<ColumnDef> filterableColumn(String field) {
    return columns().stream()
        .filter(column -> column.field.equals(field) && column.filterable)
        .findFirst();
  }

  public boolean isSortable(String field) {
    return field != null
        && columns().stream().anyMatch(column -> column.field.equals(field) && column.sortable);
  }
}
