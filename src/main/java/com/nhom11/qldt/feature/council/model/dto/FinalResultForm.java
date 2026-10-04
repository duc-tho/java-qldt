package com.nhom11.qldt.feature.council.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinalResultForm {
  @Size(max = 2000, message = "{final-results.validation.summary.max}")
  private String summaryComment;
}
