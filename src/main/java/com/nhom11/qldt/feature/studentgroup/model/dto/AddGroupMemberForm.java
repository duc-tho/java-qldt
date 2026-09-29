package com.nhom11.qldt.feature.studentgroup.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGroupMemberForm {
  @NotBlank(message = "{student-groups.validation.student-code.required}")
  private String studentCode;
}
