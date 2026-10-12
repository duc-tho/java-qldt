package com.nhom11.qldt.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev-win")
class DemoSeedDataTest {
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void seedsEightFacultiesAndFiveDemoAccounts() {
    Map<String, String> faculties = jdbcTemplate.query(
        "SELECT code, name FROM departments WHERE code IN ('CNTT', 'DDT', 'CK', 'XD', 'KT', 'NN', 'LUAT', 'KHXHNV')",
        resultSet -> {
          Map<String, String> values = new HashMap<>();
          while (resultSet.next()) {
            values.put(resultSet.getString("code"), resultSet.getString("name"));
          }
          return values;
        });
    assertEquals(Map.of(
        "CNTT", "Khoa Công nghệ thông tin",
        "DDT", "Khoa Điện - Điện tử",
        "CK", "Khoa Cơ khí",
        "XD", "Khoa Xây dựng",
        "KT", "Khoa Kinh tế",
        "NN", "Khoa Ngoại ngữ",
        "LUAT", "Khoa Luật",
        "KHXHNV", "Khoa Khoa học xã hội và Nhân văn"), faculties);

    Map<String, String> expectedRoles = Map.of(
        "admin@gmail.com", "ADMIN",
        "truongkhoa@gmail.com", "HEAD_OF_FACULTY",
        "giangvien@gmail.com", "LECTURER",
        "sinhvien1@gmail.com", "STUDENT",
        "sinhvien2@gmail.com", "STUDENT");

    for (var account : expectedRoles.entrySet()) {
      String passwordHash = jdbcTemplate.queryForObject(
          "SELECT password FROM users WHERE email = ?", String.class, account.getKey());
      assertTrue(passwordEncoder.matches("123456", passwordHash));
      Integer roleCount = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM user_roles ur JOIN users u ON u.id = ur.user_id JOIN roles r ON r.id = ur.role_id WHERE u.email = ? AND r.name = ?",
          Integer.class, account.getKey(), account.getValue());
      assertEquals(1, roleCount);
    }

    assertEquals("Trưởng khoa", jdbcTemplate.queryForObject(
        "SELECT full_name FROM users WHERE email = ?", String.class, "truongkhoa@gmail.com"));
    assertEquals("DEMO_TK", jdbcTemplate.queryForObject(
        "SELECT user_code FROM users WHERE email = ?", String.class, "truongkhoa@gmail.com"));
    assertEquals(0, jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, "truongbomon@gmail.com"));
  }
}
