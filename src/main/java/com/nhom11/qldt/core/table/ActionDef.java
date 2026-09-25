package com.nhom11.qldt.core.table;

import java.util.function.Function;

public interface ActionDef {
  String label();

  String icon();

  String getUrl(Object item);

  default boolean isDelete() {
    return false;
  }

  record Link(String label, String icon, Function<Object, String> urlExtractor)
      implements ActionDef {
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
}
