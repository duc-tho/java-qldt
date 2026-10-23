package com.nhom11.qldt.feature.department.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.department.model.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận tên và mã bộ môn từ form, đồng thời khai báo các điều kiện validation. */
public class DepartmentForm implements CrudForm<Department> {

  private Long id;

  @NotBlank(message = "{departments.validation.code.required}")
  @Size(max = 30, message = "{departments.validation.code.max}")
  private String code;

  @NotBlank(message = "{departments.validation.name.required}")
  @Size(max = 255, message = "{departments.validation.name.max}")
  private String name;

  @Override
  /** Chuyển dữ liệu biểu mẫu thành thực thể để tầng dịch vụ xử lý. */
  public Department toEntity() {
    Department department = new Department();
    department.setCode(code);
    department.setName(name);
    return department;
  }

  @Override
  /** Điền dữ liệu thực thể vào biểu mẫu để hiển thị hoặc chỉnh sửa. */
  public void fromEntity(Department entity) {
    id = entity.getId();
    code = entity.getCode();
    name = entity.getName();
  }
}
