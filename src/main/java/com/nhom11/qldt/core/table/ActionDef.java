package com.nhom11.qldt.core.table;

import java.util.List;
import java.util.function.Function;

/** Mô tả một thao tác có thể hiển thị trên từng dòng của bảng CRUD. */
public interface ActionDef {
  int INLINE_ACTION_LIMIT = 3;

  /** Trả về mã thông điệp dùng làm nhãn của thao tác. */
  String label();

  /** Trả về mã biểu tượng Iconify của thao tác. */
  String icon();

  /** Tạo URL thao tác từ bản ghi đang được hiển thị. */
  String getUrl(Object item);

  /** Cho biết thao tác có yêu cầu xác nhận xóa hay không. */
  default boolean isDelete() {
    return false;
  }

  /** Cho biết thao tác được gửi bằng yêu cầu POST hay không. */
  default boolean isPost() {
    return false;
  }

  /** Cho biết thao tác có mở luồng nhập lý do từ chối hay không. */
  default boolean isReject() {
    return false;
  }

  /** Kiểm tra thao tác có được phép hiển thị cho bản ghi cụ thể hay không. */
  default boolean isVisible(Object item) {
    return true;
  }

  /** Kiểm tra phần menu mở rộng có ít nhất một thao tác đang hiển thị hay không. */
  static boolean hasVisibleOverflow(List<ActionDef> actions, Object item) {
    return actions.stream().skip(INLINE_ACTION_LIMIT).anyMatch(action -> action.isVisible(item));
  }

  /** Thao tác điều hướng bằng liên kết thông thường. */
  record Link(
      String label,
      String icon,
      Function<Object, String> urlExtractor,
      java.util.function.Predicate<Object> visiblePredicate)
      implements ActionDef {
    public Link(String label, String icon, Function<Object, String> urlExtractor) {
      this(label, icon, urlExtractor, item -> true);
    }

    @Override
    public boolean isVisible(Object item) {
      return visiblePredicate.test(item);
    }

    @Override
    public String getUrl(Object item) {
      return urlExtractor.apply(item);
    }
  }

  /** Thao tác xóa kèm tên bản ghi để hiển thị trong hộp thoại xác nhận. */
  record Delete(
      String label,
      String icon,
      Function<Object, String> urlExtractor,
      Function<Object, String> nameExtractor,
      java.util.function.Predicate<Object> visiblePredicate)
      implements ActionDef {
    public Delete(
        String label,
        String icon,
        Function<Object, String> urlExtractor,
        Function<Object, String> nameExtractor) {
      this(label, icon, urlExtractor, nameExtractor, item -> true);
    }

    @Override
    public boolean isDelete() {
      return true;
    }

    @Override
    public boolean isVisible(Object item) {
      return visiblePredicate.test(item);
    }

    @Override
    public String getUrl(Object item) {
      return urlExtractor.apply(item);
    }

    /** Lấy tên đại diện của bản ghi để mô tả mục sắp bị xóa. */
    public String getName(Object item) {
      return nameExtractor.apply(item);
    }
  }

  /** Thao tác gửi POST trực tiếp mà không cần biểu mẫu bổ sung. */
  record Post(
      String label,
      String icon,
      Function<Object, String> urlExtractor,
      java.util.function.Predicate<Object> visiblePredicate)
      implements ActionDef {
    @Override
    public boolean isPost() {
      return true;
    }

    @Override
    public boolean isVisible(Object item) {
      return visiblePredicate.test(item);
    }

    @Override
    public String getUrl(Object item) {
      return urlExtractor.apply(item);
    }
  }

  /** Thao tác từ chối, thường yêu cầu người dùng cung cấp lý do. */
  record Reject(
      String label,
      String icon,
      Function<Object, String> urlExtractor,
      java.util.function.Predicate<Object> visiblePredicate)
      implements ActionDef {
    @Override
    public boolean isReject() {
      return true;
    }

    @Override
    public boolean isVisible(Object item) {
      return visiblePredicate.test(item);
    }

    @Override
    public String getUrl(Object item) {
      return urlExtractor.apply(item);
    }
  }
}
