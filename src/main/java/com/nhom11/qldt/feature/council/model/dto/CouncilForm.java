package com.nhom11.qldt.feature.council.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouncilForm {
  @NotBlank(message = "{councils.validation.name.required}")
  @Size(max = 150, message = "{councils.validation.name.max}")
  private String name;

  @NotEmpty(message = "{councils.validation.members.required}")
  @Size(min = 3, max = 5, message = "{councils.validation.members.size}")
  private List<Long> memberIds;

  @NotNull(message = "{councils.validation.chairperson.required}")
  private Long chairpersonId;

  @NotNull(message = "{councils.validation.secretary.required}")
  private Long secretaryId;
}
