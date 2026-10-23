package com.nhom11.qldt.feature.auth.service;

import com.nhom11.qldt.feature.auth.model.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
/** Đọc tài khoản từ database để Spring Security xác thực email, mật khẩu và quyền. */
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserService userService;

  public DatabaseUserDetailsService(UserService userService) {
    this.userService = userService;
  }

  @Override
  /** Nạp tài khoản và quyền hạn theo email để Spring Security xác thực. */
  public UserDetails loadUserByUsername(String email) {
    User user =
        userService
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
        .password(user.getPassword())
        .authorities(
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList())
        .build();
  }
}
