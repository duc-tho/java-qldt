package com.nhom11.qldt.feature.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Nhận mật khẩu hiện tại, mật khẩu mới và phần xác nhận từ form đổi mật khẩu. */
public class PasswordChangeForm {

  @NotBlank(message = "{account.validation.current_password.required}")
  private String currentPassword;

  @NotBlank(message = "{account.validation.new_password.required}")
  @Size(min = 6, message = "{users.validation.password.min}")
  private String newPassword;

  @NotBlank(message = "{account.validation.confirm_password.required}")
  private String confirmPassword;
}
