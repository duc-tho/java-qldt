package com.nhom11.qldt.feature.announcement.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.Role;
import com.nhom11.qldt.feature.auth.repository.RoleRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnnouncementFormRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private RoleRepository roles;

  @Test
  @WithMockUser(roles = "ADMIN")
  void sharedFieldsRenderValidationTargetsAndAudienceCheckbox() throws Exception {
    Role student = new Role();
    student.setId(1L);
    student.setName("STUDENT");
    when(roles.findAll()).thenReturn(List.of(student));

    mockMvc.perform(get("/announcements/new"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"title-error\"")))
        .andExpect(content().string(containsString("id=\"content-error\"")))
        .andExpect(content().string(containsString("name=\"roleIds\"")));
  }
}
