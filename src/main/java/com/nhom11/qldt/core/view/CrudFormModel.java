package com.nhom11.qldt.core.view;

import java.util.Map;

public record CrudFormModel<T>(
    T data,
    String actionUrl,
    String method,
    String backUrl,
    boolean readonly,
    String fieldsTemplate,
    Map<String, String> validationRules) {}
