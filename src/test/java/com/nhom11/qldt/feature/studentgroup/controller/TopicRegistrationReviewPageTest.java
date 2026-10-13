package com.nhom11.qldt.feature.studentgroup.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.GroupActivity;
import com.nhom11.qldt.feature.studentgroup.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.studentgroup.service.GroupActivityService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
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
  @MockitoBean private StudentGroupService studentGroupService;
  @MockitoBean private UserService userService;
  @MockitoBean private GroupActivityService activityService;

  @Test
  void listShowsReviewedGroupsWithTheirStatusAndDetailAction() throws Exception {
    stubRegistrations();

    mockMvc.perform(get("/topic-registrations"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Approved group")))
        .andExpect(content().string(containsString("Rejected group")))
        .andExpect(content().string(containsString("name=\"status\"")))
        .andExpect(content().string(containsString("badge-success")))
        .andExpect(content().string(containsString("badge-error")))
        .andExpect(content().string(containsString("Xem chi tiết")));
  }

  @Test
  void statusFilterShowsOnlyMatchingGroups() throws Exception {
    stubRegistrations();

    mockMvc.perform(get("/topic-registrations").param("status", "APPROVED"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Approved group")))
        .andExpect(content().string(not(containsString("Rejected group"))));
  }

  @Test
  void lecturerDetailShowsReviewHistory() throws Exception {
    User lecturer = new User();
    lecturer.setId(7L);
    lecturer.setFullName("Lecturer One");
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    TopicRegistration registration = registration("Reviewed group", TopicRegistrationStatus.REJECTED);
    registration.setId(11L);
    when(studentGroupService.adviserRegistration(11L, lecturer)).thenReturn(registration);
    GroupActivity activity = new GroupActivity();
    activity.setActor(lecturer);
    activity.setType(GroupActivityType.REJECTED);
    activity.setDetail("Missing details");
    activity.setOccurredAt(LocalDateTime.now());
    when(activityService.history(2L)).thenReturn(List.of(activity));

    mockMvc.perform(get("/topic-registrations/11"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Missing details")))
        .andExpect(content().string(containsString("Lý do:")))
        .andExpect(content().string(containsString("Đăng ký bị từ chối")));
  }

  private void stubRegistrations() {
    User lecturer = new User();
    lecturer.setId(7L);
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    when(studentGroupService.adviserRegistrations(lecturer)).thenReturn(List.of(
        registration("Approved group", TopicRegistrationStatus.APPROVED),
        registration("Rejected group", TopicRegistrationStatus.REJECTED)));
  }

  private TopicRegistration registration(String groupName, TopicRegistrationStatus status) {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("2026-2027");
    StudentGroup group = new StudentGroup();
    group.setId(2L);
    group.setName(groupName);
    group.setRegistrationPeriod(period);
    Department department = new Department();
    department.setName("Khoa Công nghệ thông tin");
    Topic topic = new Topic();
    topic.setTitle("Example topic");
    topic.setDepartment(department);
    TopicRegistration registration = new TopicRegistration();
    registration.setStudentGroup(group);
    registration.setTopic(topic);
    registration.setStatus(status);
    return registration;
  }
}
