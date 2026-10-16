package com.nhom11.qldt.core.table;

import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class TableModelFactory {

  public <T> TableModel<T> create(
      Page<T> data, String baseUrl, TableQuery query, BaseTableDefinition<T> definition) {
    Pageable pageable = query.pageable(definition);
    Sort.Order sortOrder = pageable.getSort().stream().findFirst().orElse(null);
    String sortField = sortOrder == null ? "" : sortOrder.getProperty();
    String sortDirection =
        sortOrder == null
            ? definition.defaultSortDirection()
            : sortOrder.getDirection().name().toLowerCase(Locale.ROOT);
    String search = query.getSearch() == null ? "" : query.getSearch();
    Map<String, String> filters = query.filterValues(definition);

    return new TableModel<>(
        data,
        baseUrl,
        definition.createUrl(baseUrl),
        definition.bulkDeleteUrl(baseUrl),
        search,
        sortField,
        sortDirection,
        pageable.getPageSize(),
        filters,
        definition.pageSizeOptions(),
        definition.columns(),
        definition.actions(baseUrl),
        definition.bulkDeleteEnabled(),
        definition.canCreate());
  }
}
