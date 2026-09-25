package com.nhom11.qldt.core.table;

import java.util.function.Function;

public class ColumnDef {
  public final String field;
  public final String label;
  public final boolean searchable;
  public final boolean filterable;
  public final boolean sortable;
  private final Function<Object, String> valueExtractor;

  public ColumnDef(
      String field,
      String label,
      Function<Object, String> valueExtractor,
      boolean searchable,
      boolean filterable,
      boolean sortable) {
    this.field = field;
    this.label = label;
    this.valueExtractor = valueExtractor;
    this.searchable = searchable;
    this.filterable = filterable;
    this.sortable = sortable;
  }

  public String getValue(Object item) {
    return valueExtractor.apply(item);
  }
}
