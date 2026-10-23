package com.nhom11.qldt.feature.department.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.department.model.entity.Department;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
/** Khai báo các cột, nút thao tác và quyền hiển thị trên bảng bộ môn. */
public class DepartmentTableDefinition extends BaseTableDefinition<Department> {

  @Override
  /** Khai báo các cột và khả năng tìm kiếm, lọc, sắp xếp của bảng. */
  public List<ColumnDef> columns() {
    return List.of(
        new ColumnDef(
            "id",
            "departments.column.id",
            item -> String.valueOf(((Department) item).getId()),
            false,
            false,
            true),
        new ColumnDef(
            "code",
            "departments.column.code",
            item -> ((Department) item).getCode(),
            true,
            true,
            true),
        new ColumnDef(
            "name",
            "departments.column.name",
            item -> ((Department) item).getName(),
            true,
            true,
            true));
  }

  @Override
  /** Khai báo các thao tác hiển thị cho từng bản ghi trong bảng. */
  public List<ActionDef> actions(String baseUrl) {
    return List.of(
        new ActionDef.Link(
            "crud.action.edit",
            "bi:pencil-square",
            item -> baseUrl + "/" + ((Department) item).getId() + "/edit"),
        new ActionDef.Delete(
            "crud.action.delete",
            "bi:trash",
            item -> baseUrl + "/" + ((Department) item).getId() + "/delete",
            item -> ((Department) item).getName()));
  }
}
