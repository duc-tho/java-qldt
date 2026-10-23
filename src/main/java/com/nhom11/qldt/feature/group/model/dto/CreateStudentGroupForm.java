package com.nhom11.qldt.feature.group.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận tên nhóm từ form tạo nhóm sinh viên. */
public class CreateStudentGroupForm {
  @NotBlank(message = "{student-groups.validation.name.required}")
  @Size(max = 150, message = "{student-groups.validation.name.max}")
  private String name;
}
