package com.nhom11.qldt.core.view;

import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.TableModelFactory;
import com.nhom11.qldt.core.table.TableQuery;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class CrudViewSupport {

  private final TableModelFactory tableModelFactory;

  public CrudViewSupport(TableModelFactory tableModelFactory) {
    this.tableModelFactory = tableModelFactory;
  }

  public <T> String table(
      Model model,
      Page<T> data,
      String baseUrl,
      TableQuery query,
      BaseTableDefinition<T> definition) {

    model.addAttribute("table", tableModelFactory.create(data, baseUrl, query, definition));

    return "fragment/crud/table :: crud-table";
  }

  public <T> void form(
      Model model,
      T data,
      String actionUrl,
      String method,
      String backUrl,
      boolean readonly,
      String fieldsTemplate,
      Map<String, String> validationRules) {

    model.addAttribute(
        "form",
        new CrudFormModel<>(
            data, actionUrl, method, backUrl, readonly, fieldsTemplate, validationRules));
  }
}
