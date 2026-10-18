package com.nhom11.qldt.feature.group.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
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
@WithMockUser(username = "lecturer", roles = "LECTURER")
class TopicRegistrationReviewPageTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private StudentGroupService groupService;
  @MockitoBean private UserService userService;

  @Test
  void legacyListRedirectsToPeriodWorkFilter() throws Exception {
    mockMvc.perform(get("/topic-registrations"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dot?congViec=duyet"));
  }

  @Test
  void legacyDetailRedirectsToCanonicalGroup() throws Exception {
    User lecturer = new User();
    lecturer.setId(7L);
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    StudentGroup group = new StudentGroup();
    group.setId(2L);
    group.setRegistrationPeriod(period);
    TopicRegistration registration = new TopicRegistration();
    registration.setStudentGroup(group);
    when(groupService.adviserRegistration(11L, lecturer)).thenReturn(registration);

    mockMvc.perform(get("/topic-registrations/11"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dot/1/nhom/2"));
  }

  @Test
  void legacyApprovePostIsRemoved() throws Exception {
    mockMvc.perform(post("/topic-registrations/11/approve").with(csrf()))
        .andExpect(status().isNotFound());
  }
}
