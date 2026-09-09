package com.nhom11.qldt.feature.auth.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.auth.model.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm implements CrudForm<User> {

    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự")
    private String username;

    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 50, message = "Họ tên tối đa 50 ký tự")
    private String fullName;

    @Override
    public User toEntity() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        }
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(fullName);
        return user;
    }

    @Override
    public void fromEntity(User entity) {
        this.id = entity.getId();
        this.username = entity.getUsername();
        this.email = entity.getEmail();
        this.phone = entity.getPhone();
        this.fullName = entity.getFullName();
    }
}
