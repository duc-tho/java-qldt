package com.nhom11.qldt.feature.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class UserRoleFormTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;

  @Test
  void detailShowsSelectableAccountDataInsteadOfEditableInputs() throws Exception {
    User existing = new User();
    existing.setId(9L);
    existing.setFullName("Test User");
    existing.setUsername("test-user");
    existing.setEmail("test-user@example.com");
    when(userService.findById(9L)).thenReturn(existing);

    mockMvc.perform(get("/users/9"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-readonly-field=\"fullName\"")))
        .andExpect(content().string(containsString("Test User")))
        .andExpect(content().string(not(containsString("<input id=\"fullName\""))));
  }

  @Test
  void createShowsRoleErrorWithoutSavingInvalidCombination() throws Exception {
    when(userService.create(any(), any()))
        .thenThrow(new IllegalArgumentException("users.validation.roles.invalid"));

    mockMvc.perform(post("/users").with(csrf())
            .param("fullName", "Test User")
            .param("username", "test-user")
            .param("email", "test-user@example.com")
            .param("password", "123456")
            .param("roleIds", "1", "2"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Chỉ chọn một vai trò")));
  }

  @Test
  void editShowsRoleErrorWithoutSavingInvalidCombination() throws Exception {
    User existing = new User();
    existing.setId(9L);
    when(userService.findById(9L)).thenReturn(existing);
    when(userService.update(any(), any()))
        .thenThrow(new IllegalArgumentException("users.validation.roles.invalid"));

    mockMvc.perform(post("/users/9").with(csrf())
            .param("fullName", "Test User")
            .param("username", "test-user")
            .param("email", "test-user@example.com")
            .param("roleIds", "1", "2"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Chỉ chọn một vai trò")));
  }
}
