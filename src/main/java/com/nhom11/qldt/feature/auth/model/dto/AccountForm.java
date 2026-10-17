package com.nhom11.qldt.feature.auth.model.dto;

import com.nhom11.qldt.feature.auth.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountForm {

  private String email;

  private String userCode;

  @Size(max = 20, message = "{users.validation.phone.max}")
  @Pattern(regexp = "^$|^0\\d{9}$", message = "{users.validation.phone.invalid}")
  private String phone;

  @NotBlank(message = "{users.validation.full_name.required}")
  @Size(max = 50, message = "{users.validation.full_name.max}")
  private String fullName;

  public void fromEntity(User user) {
    email = user.getEmail();
    userCode = user.getUserCode();
    phone = user.getPhone();
    fullName = user.getFullName();
  }
}
