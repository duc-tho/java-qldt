package com.nhom11.qldt.feature.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountFormRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;

  @Test
  @WithMockUser(username = "test@example.com", roles = "STUDENT")
  void accountFormMarksRequiredFieldsAndRendersPasswordDialog() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setFullName("Test User");
    user.setEmail("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    mockMvc.perform(get("/account"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-required-for=\"account-full-name\"")))
        .andExpect(content().string(containsString("data-required-for=\"account-email\"")))
        .andExpect(content().string(containsString("data-required-for=\"new-password\"")))
        .andExpect(content().string(containsString("change-password-dialog")));
  }

  @Test
  void loginFormKeepsValidationTargetsAndInputs() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"login-email-error\"")))
        .andExpect(content().string(containsString("id=\"login-password-error\"")))
        .andExpect(content().string(containsString("name=\"email\"")))
        .andExpect(content().string(containsString("name=\"password\"")));
  }
}
