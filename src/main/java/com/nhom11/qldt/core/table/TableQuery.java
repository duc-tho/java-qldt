package com.nhom11.qldt.core.table;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Nhận và chuẩn hóa các tham số tìm kiếm, lọc, sắp xếp và phân trang từ HTTP. */
public class TableQuery {
  private String search;
  private Integer page;
  private String sort;
  private String dir;
  private Integer size;
  private Map<String, String> filters = new LinkedHashMap<>();

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Map<String, String> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, String> filters) {
    this.filters = filters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(filters);
  }

  /** Trả về số trang hợp lệ hoặc giá trị mặc định của định nghĩa bảng. */
  public int pageNumber(BaseTableDefinition<?> definition) {
    return page == null || page < 0 ? definition.defaultPage() : page;
  }

  /** Chỉ chấp nhận kích thước trang nằm trong danh sách được cấu hình. */
  public int pageSize(BaseTableDefinition<?> definition) {
    return size != null && definition.pageSizeOptions().contains(size)
        ? size
        : definition.defaultPageSize();
  }

  /** Chỉ trả về trường sắp xếp khi cột tương ứng cho phép sắp xếp. */
  public String sortField(BaseTableDefinition<?> definition) {
    if (definition.isSortable(sort)) {
      return sort;
    }

    return definition.isSortable(definition.defaultSort()) ? definition.defaultSort() : "";
  }

  /** Chuẩn hóa chiều sắp xếp về {@code asc} hoặc {@code desc}. */
  public String sortDirection(BaseTableDefinition<?> definition) {
    if ("desc".equalsIgnoreCase(dir)) {
      return "desc";
    }
    if ("asc".equalsIgnoreCase(dir)) {
      return "asc";
    }

    return "desc".equalsIgnoreCase(definition.defaultSortDirection()) ? "desc" : "asc";
  }

  /** Loại bỏ bộ lọc rỗng hoặc không được định nghĩa và chuẩn hóa khoảng trắng. */
  public Map<String, String> filterValues(BaseTableDefinition<?> definition) {
    Map<String, String> allowedFilters = new LinkedHashMap<>();

    // Chỉ chuyển tiếp các trường được định nghĩa là có thể lọc để tránh truy vấn tùy ý.
    filters.forEach(
        (field, value) -> {
          if (value != null && !value.isBlank() && definition.filterableColumn(field).isPresent()) {
            allowedFilters.put(field, value.trim());
          }
        });

    return allowedFilters;
  }

  /** Tạo đối tượng phân trang Spring từ các tham số đã được kiểm tra. */
  public Pageable pageable(BaseTableDefinition<?> definition) {
    String sortField = sortField(definition);
    Sort sortSpec = Sort.unsorted();

    // Không tạo Sort khi bảng không có trường sắp xếp hợp lệ.
    if (!sortField.isBlank()) {
      Sort.Direction direction =
          "desc".equals(sortDirection(definition)) ? Sort.Direction.DESC : Sort.Direction.ASC;
      sortSpec = Sort.by(direction, sortField);
    }

    return PageRequest.of(pageNumber(definition), pageSize(definition), sortSpec);
  }
}
