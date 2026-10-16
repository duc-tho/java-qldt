package com.nhom11.qldt.feature.council.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ResultPageRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private CouncilService councilService;
  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private StudentGroupService groupService;
  @MockitoBean private UserService userService;

  private User viewer;
  private RegistrationPeriod period;

  @BeforeEach
  void setUp() {
    viewer = new User();
    viewer.setId(2L);
    period = new RegistrationPeriod();
    period.setId(3L);
    period.setName("Semester One");
    when(periodService.findById(3L)).thenReturn(period);
    when(userService.findByEmail("viewer")).thenReturn(Optional.of(viewer));
  }

  @Test
  @WithMockUser(username = "viewer", roles = "STUDENT")
  void unpublishedResultsAreNotRendered() throws Exception {
    mockMvc.perform(get("/dot/3/ket-qua"))
        .andExpect(status().isNotFound());

    verify(groupService, never()).groupSummariesForStudent(viewer);
  }

  @Test
  @WithMockUser(username = "viewer", roles = "HEAD_OF_FACULTY")
  void staffCanOpenPublicationPageBeforePublishing() throws Exception {
    mockMvc.perform(get("/dot/3/ket-qua"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("/dot/3/ket-qua/cong-bo")));
  }

  @Test
  @WithMockUser(username = "viewer", roles = "STUDENT")
  void studentOnlyReceivesResultsForOwnGroups() throws Exception {
    when(councilService.resultsPublished(3L)).thenReturn(true);
    when(periodService.findVisibleForStudent(2L)).thenReturn(List.of(period));
    StudentGroup group = group(4L);
    when(groupService.groupSummariesForStudent(viewer))
        .thenReturn(List.of(new GroupSummary(group, "Student", "SV01", 1, List.of(viewer), null)));
    when(councilService.publishedResults(3L, List.of(4L)))
        .thenReturn(List.of(result(group, "Allowed topic")));

    mockMvc.perform(get("/dot/3/ket-qua"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Allowed topic")))
        .andExpect(content().string(containsString("/dot/3/nhom/4")));

    verify(councilService).publishedResults(3L, List.of(4L));
  }

  @Test
  @WithMockUser(username = "viewer", roles = {"LECTURER", "STUDENT"})
  void multipleRolesIncludeLecturerAndStudentGroups() throws Exception {
    when(councilService.resultsPublished(3L)).thenReturn(true);
    when(periodService.findVisibleForLecturer(2L)).thenReturn(List.of(period));
    when(periodService.findVisibleForStudent(2L)).thenReturn(List.of(period));
    StudentGroup assigned = group(4L);
    StudentGroup own = group(5L);
    when(groupService.groupSummariesForLecturer(3L, viewer, false))
        .thenReturn(List.of(new GroupSummary(assigned, "Other", "SV02", 1, List.of(), null)));
    when(groupService.groupSummariesForStudent(viewer))
        .thenReturn(List.of(new GroupSummary(own, "Viewer", "SV01", 1, List.of(viewer), null)));

    mockMvc.perform(get("/dot/3/ket-qua"))
        .andExpect(status().isOk());

    verify(councilService).publishedResults(3L, List.of(4L, 5L));
  }

  private StudentGroup group(Long id) {
    StudentGroup group = new StudentGroup();
    group.setId(id);
    group.setName("Group " + id);
    group.setRegistrationPeriod(period);
    return group;
  }

  private FinalResult result(StudentGroup group, String title) {
    Topic topic = new Topic();
    topic.setTitle(title);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStudentGroup(group);
    FinalResult result = new FinalResult();
    result.setTopicRegistration(registration);
    result.setFinalScore(new BigDecimal("8.50"));
    result.setSummaryComment("Passed");
    return result;
  }
}
