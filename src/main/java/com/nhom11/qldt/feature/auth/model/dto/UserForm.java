package com.nhom11.qldt.feature.auth.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.auth.model.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm implements CrudForm<User> {

    private Long id;

    @NotBlank(message = "{users.validation.username.required}")
    @Size(max = 50, message = "{users.validation.username.max}")
    private String username;

    @Pattern(regexp = "^$|^.{6,}$", message = "{users.validation.password.min}")
    private String password;

    @NotBlank(message = "{users.validation.email.required}")
    @Email(message = "{users.validation.email.invalid}")
    private String email;

    @Size(max = 20, message = "{users.validation.phone.max}")
    private String phone;

    @NotBlank(message = "{users.validation.full_name.required}")
    @Size(max = 50, message = "{users.validation.full_name.max}")
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
