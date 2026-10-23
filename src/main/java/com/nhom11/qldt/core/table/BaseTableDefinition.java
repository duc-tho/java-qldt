package com.nhom11.qldt.core.table;

import java.util.List;
import java.util.Optional;

/** Định nghĩa cấu trúc, quyền thao tác và giá trị mặc định của một bảng CRUD. */
public abstract class BaseTableDefinition<T> {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final String DEFAULT_SORT = "";
  private static final String DEFAULT_SORT_DIRECTION = "asc";
  private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(10, 20, 50);

  /** Khai báo các cột được hiển thị cùng khả năng tìm kiếm, lọc và sắp xếp. */
  public abstract List<ColumnDef> columns();

  /** Khai báo các thao tác áp dụng cho từng dòng dữ liệu. */
  public abstract List<ActionDef> actions(String baseUrl);

  /** Cho biết bảng có cho phép xóa đồng thời nhiều bản ghi hay không. */
  public boolean bulkDeleteEnabled() {
    return false;
  }

  /** Cho biết người dùng hiện tại có thể tạo bản ghi mới hay không. */
  public boolean canCreate() {
    return true;
  }

  /** Tạo URL mặc định của trang tạo mới. */
  public String createUrl(String baseUrl) {
    return baseUrl + "/new";
  }

  /** Tạo URL mặc định của thao tác xóa hàng loạt. */
  public String bulkDeleteUrl(String baseUrl) {
    return baseUrl + "/delete";
  }

  /** Trả về chỉ số trang mặc định, bắt đầu từ 0. */
  public int defaultPage() {
    return DEFAULT_PAGE;
  }

  /** Trả về số bản ghi mặc định trên mỗi trang. */
  public int defaultPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  /** Trả về trường sắp xếp mặc định; chuỗi rỗng nghĩa là không sắp xếp. */
  public String defaultSort() {
    return DEFAULT_SORT;
  }

  /** Trả về chiều sắp xếp mặc định. */
  public String defaultSortDirection() {
    return DEFAULT_SORT_DIRECTION;
  }

  /** Trả về các kích thước trang mà giao diện cho phép lựa chọn. */
  public List<Integer> pageSizeOptions() {
    return PAGE_SIZE_OPTIONS;
  }

  /** Kiểm tra trường có được khai báo cho phép tìm kiếm hay không. */
  public boolean isSearchable(String field) {
    return columns().stream().anyMatch(column -> column.field.equals(field) && column.searchable);
  }

  /** Tìm định nghĩa cột có thể lọc theo tên trường. */
  public Optional<ColumnDef> filterableColumn(String field) {
    return columns().stream()
        .filter(column -> column.field.equals(field) && column.filterable)
        .findFirst();
  }

  /** Kiểm tra trường có tồn tại và được phép sắp xếp hay không. */
  public boolean isSortable(String field) {
    return field != null
        && columns().stream().anyMatch(column -> column.field.equals(field) && column.sortable);
  }
}
