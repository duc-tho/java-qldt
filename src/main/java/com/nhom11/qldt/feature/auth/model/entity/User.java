package com.nhom11.qldt.feature.auth.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
/** Biểu diễn tài khoản người dùng cùng hồ sơ, vai trò và thông tin xác thực. */
public final class User extends SoftDeleteEntity {
  @Column(nullable = false, unique = true, length = 50)
  String username;

  @Column(nullable = false, length = 255)
  String password;

  @Column(unique = true, length = 50, nullable = false)
  String email;

  @Column(unique = true, length = 20)
  String phone;

  @Column(name = "full_name", nullable = false, length = 50)
  String fullName;

  @Column(name = "user_code", unique = true, length = 30)
  String userCode;

  @ManyToMany
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();
}
