package com.nhom11.qldt.core.view;

import java.util.Map;

/** Dữ liệu cấu hình để fragment Thymeleaf dựng một biểu mẫu CRUD. */
public record CrudFormModel<T>(
    T data,
    String actionUrl,
    String method,
    String backUrl,
    String layoutClass,
    boolean readonly,
    String fieldsTemplate,
    Map<String, String> validationRules) {}
