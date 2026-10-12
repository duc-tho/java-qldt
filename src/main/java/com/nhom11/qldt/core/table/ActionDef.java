package com.nhom11.qldt.core.table;

import java.util.function.Function;

public interface ActionDef {
  String label();

  String icon();

  String getUrl(Object item);

  default boolean isDelete() {
    return false;
  }

  default boolean isPost() {
    return false;
  }

  default boolean isReject() {
    return false;
  }

  default boolean isVisible(Object item) {
    return true;
  }

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

  record Delete(
      String label,
      String icon,
      Function<Object, String> urlExtractor,
      Function<Object, String> nameExtractor)
      implements ActionDef {
    @Override
    public boolean isDelete() {
      return true;
    }

    @Override
    public String getUrl(Object item) {
      return urlExtractor.apply(item);
    }

    public String getName(Object item) {
      return nameExtractor.apply(item);
    }
  }

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
