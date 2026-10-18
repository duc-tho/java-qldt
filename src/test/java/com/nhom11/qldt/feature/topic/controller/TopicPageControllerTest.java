package com.nhom11.qldt.feature.topic.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.time.LocalDateTime;
import java.util.List;
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
class TopicPageControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private TopicService topicService;
  @MockitoBean private UserService userService;
  @MockitoBean private DepartmentService departmentService;
  @MockitoBean private StudentGroupService studentGroupService;

  @Test
  @WithMockUser(username = "student@example.com", roles = "STUDENT")
  void studentListOnlyUsesApprovedTopics() throws Exception {
    visiblePeriod();
    Topic approved = topic(TopicStatus.APPROVED);
    when(topicService.findApprovedByRegistrationPeriodId(1L, null, null))
        .thenReturn(List.of(approved));
    when(departmentService.findAll()).thenReturn(List.of(approved.getDepartment()));

    mockMvc.perform(get("/dot/1/de-tai"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Visible topic")))
        .andExpect(content().string(not(containsString("Private proposal"))));

    verify(topicService).findApprovedByRegistrationPeriodId(1L, null, null);
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = "STUDENT")
  void studentCannotOpenUnapprovedTopicDirectly() throws Exception {
    visiblePeriod();
    Topic proposal = topic(TopicStatus.PROPOSED);
    when(topicService.findByIdAndRegistrationPeriodId(2L, 1L)).thenReturn(proposal);

    mockMvc.perform(get("/dot/1/de-tai/2"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = {"LECTURER", "STUDENT"})
  void multipleRolesUseStudentTopicsWhenLecturerPeriodIsNotVisible() throws Exception {
    visiblePeriod();
    Topic approved = topic(TopicStatus.APPROVED);
    when(topicService.findApprovedByRegistrationPeriodId(1L, null, null))
        .thenReturn(List.of(approved));

    mockMvc.perform(get("/dot/1/de-tai"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Visible topic")));

    verify(topicService).findApprovedByRegistrationPeriodId(1L, null, null);
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = {"LECTURER", "STUDENT"})
  void studentGroupSelectionWorksWithMultipleRoles() throws Exception {
    User student = visiblePeriod();
    RegistrationPeriod period = periodService.findVisibleForStudent(7L).getFirst();
    StudentGroup group = new StudentGroup();
    group.setId(4L);
    group.setRegistrationPeriod(period);
    GroupMember leader = new GroupMember();
    leader.setLeader(true);
    leader.setStudent(student);
    when(studentGroupService.findGroup(4L, 1L)).thenReturn(group);
    when(studentGroupService.findCurrentGroup(1L, student)).thenReturn(group);
    when(studentGroupService.members(4L)).thenReturn(List.of(leader));
    when(studentGroupService.isOpen(period)).thenReturn(true);
    Topic approved = topic(TopicStatus.APPROVED);
    when(topicService.findApprovedByRegistrationPeriodId(1L, null, null))
        .thenReturn(List.of(approved));

    mockMvc.perform(get("/dot/1/de-tai").param("nhomId", "4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("action=\"/dot/1/nhom/4/de-tai\"")));
  }

  @Test
  @WithMockUser(roles = "HEAD_OF_FACULTY")
  void topicFormRendersSharedInputSelectAndTextarea() throws Exception {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Period");
    when(periodService.findById(1L)).thenReturn(period);
    when(topicService.canManageTopicsAsStaff(period)).thenReturn(true);

    mockMvc.perform(get("/dot/1/de-tai/tao"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"title\"")))
        .andExpect(content().string(containsString("data-required-for=\"title\"")))
        .andExpect(content().string(containsString("id=\"title-error\"")))
        .andExpect(content().string(containsString("id=\"departmentId\"")))
        .andExpect(content().string(containsString("data-required-for=\"departmentId\"")))
        .andExpect(content().string(containsString("id=\"description\"")))
        .andExpect(content().string(containsString("id=\"description-error\"")));
  }

  @Test
  @WithMockUser(roles = "HEAD_OF_FACULTY")
  void departmentFormRendersSharedInput() throws Exception {
    mockMvc.perform(get("/departments/new"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"code\"")))
        .andExpect(content().string(containsString("id=\"name\"")));
  }

  @Test
  @WithMockUser(roles = "HEAD_OF_FACULTY")
  void departmentDetailShowsSelectableTextWithoutEditControls() throws Exception {
    Department department = new Department();
    department.setId(3L);
    department.setCode("CS");
    department.setName("Computer Science");
    when(departmentService.findById(3L)).thenReturn(department);

    mockMvc.perform(get("/departments/3"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-readonly-field=\"code\"")))
        .andExpect(content().string(containsString("select-text")))
        .andExpect(content().string(not(containsString("<input id=\"code\""))));
  }

  @Test
  @WithMockUser(roles = "HEAD_OF_FACULTY")
  void topicDetailShowsSelectedDepartmentAndDescriptionAsText() throws Exception {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Period");
    when(periodService.findById(1L)).thenReturn(period);
    Topic topic = topic(TopicStatus.APPROVED);
    topic.setRegistrationPeriod(period);
    when(topicService.findByIdAndRegistrationPeriodId(2L, 1L)).thenReturn(topic);

    mockMvc.perform(get("/dot/1/de-tai/2"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-readonly-field=\"departmentId\"")))
        .andExpect(content().string(containsString("Computer Science")))
        .andExpect(content().string(containsString("data-readonly-field=\"description\"")));
  }

  @Test
  @WithMockUser(roles = "HEAD_OF_FACULTY")
  void proposedTopicShowsRejectionReasonFieldInDialog() throws Exception {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Period");
    when(periodService.findById(1L)).thenReturn(period);
    when(topicService.canManageTopicsAsStaff(period)).thenReturn(true);
    Topic topic = topic(TopicStatus.PROPOSED);
    topic.setRegistrationPeriod(period);
    when(topicService.findByIdAndRegistrationPeriodId(2L, 1L)).thenReturn(topic);

    mockMvc.perform(get("/dot/1/de-tai/2"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"reject_topic\"")))
        .andExpect(content().string(containsString("id=\"reason\"")))
        .andExpect(content().string(containsString("maxlength=\"500\"")))
        .andExpect(content().string(containsString("/de-tai/2/tu-choi")));
  }

  private User visiblePeriod() {
    User student = new User();
    student.setId(7L);
    when(userService.findByEmail("student@example.com")).thenReturn(Optional.of(student));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Period");
    period.setStudentRegistrationStart(LocalDateTime.now().minusDays(1));
    when(periodService.findVisibleForStudent(7L)).thenReturn(List.of(period));
    return student;
  }

  private Topic topic(TopicStatus status) {
    Department department = new Department();
    department.setId(3L);
    department.setName("Computer Science");
    Topic topic = new Topic();
    topic.setId(2L);
    topic.setTitle("Visible topic");
    topic.setDescription("Description");
    topic.setStatus(status);
    topic.setDepartment(department);
    topic.setAdvisers(java.util.Set.of());
    return topic;
  }
}
