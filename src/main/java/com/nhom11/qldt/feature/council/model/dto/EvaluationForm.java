package com.nhom11.qldt.feature.council.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationForm {
  @DecimalMin(value = "0.00", message = "{evaluations.validation.score.range}")
  @DecimalMax(value = "10.00", message = "{evaluations.validation.score.range}")
  private BigDecimal score;

  @Size(max = 2000, message = "{evaluations.validation.comment.max}")
  private String comment;
}
