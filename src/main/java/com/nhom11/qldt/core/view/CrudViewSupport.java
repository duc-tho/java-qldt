package com.nhom11.qldt.core.view;

import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.TableModelFactory;
import com.nhom11.qldt.core.table.TableQuery;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
/** Hỗ trợ controller chuẩn bị model dùng chung cho bảng và biểu mẫu CRUD. */
public class CrudViewSupport {

  private final TableModelFactory tableModelFactory;

  public CrudViewSupport(TableModelFactory tableModelFactory) {
    this.tableModelFactory = tableModelFactory;
  }

  /** Gắn model bảng vào view và trả về fragment bảng CRUD cần render. */
  public <T> String table(
      Model model,
      Page<T> data,
      String baseUrl,
      TableQuery query,
      BaseTableDefinition<T> definition) {

    model.addAttribute("table", tableModelFactory.create(data, baseUrl, query, definition));

    return "fragment/crud/table :: crud-table";
  }

  /** Chuẩn bị biểu mẫu với chiều rộng mặc định của giao diện. */
  public <T> void form(
      Model model,
      T data,
      String actionUrl,
      String method,
      String backUrl,
      boolean readonly,
      String fieldsTemplate,
      Map<String, String> validationRules) {

    form(
        model,
        data,
        actionUrl,
        method,
        backUrl,
        "w-full lg:w-1/2 mx-auto",
        readonly,
        fieldsTemplate,
        validationRules);
  }

  /** Chuẩn bị đầy đủ dữ liệu và cấu hình hiển thị cho biểu mẫu CRUD. */
  public <T> void form(
      Model model,
      T data,
      String actionUrl,
      String method,
      String backUrl,
      String layoutClass,
      boolean readonly,
      String fieldsTemplate,
      Map<String, String> validationRules) {

    model.addAttribute("data", data);
    model.addAttribute(
        "form",
        new CrudFormModel<>(
            data,
            actionUrl,
            method,
            backUrl,
            layoutClass,
            readonly,
            fieldsTemplate,
            validationRules));
  }
}
