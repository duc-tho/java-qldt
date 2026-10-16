package com.nhom11.qldt.feature.group.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import com.nhom11.qldt.feature.group.model.entity.GroupActivity;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.service.GroupActivityService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
@WithMockUser(username = "student", roles = "STUDENT")
class StudentGroupPageRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private StudentGroupService studentGroupService;
  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private UserService userService;
  @MockitoBean private GroupActivityService activityService;
  @MockitoBean private ReportSubmissionService reportService;
  @MockitoBean private CouncilService councilService;

  private User student;
  private RegistrationPeriod period;
  private StudentGroup group;

  @BeforeEach
  void setUp() {
    student = new User();
    student.setId(2L);
    student.setFullName("Student One");
    when(userService.findByEmail("student")).thenReturn(Optional.of(student));

    period = new RegistrationPeriod();
    period.setId(3L);
    period.setName("Semester One");
    period.setStudentRegistrationEnd(LocalDateTime.now().plusDays(1));
    group = new StudentGroup();
    group.setId(4L);
    group.setName("Group Four");
    group.setRegistrationPeriod(period);
  }

  @Test
  void groupDetailRendersEmptyTopicAndActivity() throws Exception {
    when(studentGroupService.findGroup(4L, 3L)).thenReturn(group);
    when(studentGroupService.canViewGroup(group, student)).thenReturn(true);
    when(studentGroupService.members(4L)).thenReturn(List.of());
    when(studentGroupService.isOpen(period)).thenReturn(true);
    when(activityService.history(4L)).thenReturn(List.of());

    mockMvc.perform(get("/dot/3/nhom/4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Four")))
        .andExpect(content().string(containsString("Trạng thái đăng ký: Chưa chọn đề tài")))
        .andExpect(content().string(containsString("Hạn đăng ký:")))
        .andExpect(content().string(containsString("Mã nhóm: #4")))
        .andExpect(content().string(containsString("Đợt Semester One")))
        .andExpect(content().string(containsString("Nhật ký nhóm")));
  }

  @Test
  void groupDetailShowsTopicAdvisersAndReviewHistory() throws Exception {
    User lecturer = new User();
    lecturer.setId(5L);
    lecturer.setFullName("Lecturer One");
    lecturer.setEmail("lecturer@example.test");
    lecturer.setUserCode("GV01");
    Department department = new Department();
    department.setName("Computer Science");
    Topic topic = new Topic();
    topic.setId(7L);
    topic.setTitle("Network Analysis");
    topic.setDescription("Inspect network traffic");
    topic.setDepartment(department);
    topic.setAdvisers(Set.of(lecturer));
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.PENDING);
    GroupMember leader = new GroupMember();
    leader.setLeader(true);
    leader.setStudent(student);
    GroupActivity activity = new GroupActivity();
    activity.setActor(lecturer);
    activity.setType(GroupActivityType.APPROVED);
    activity.setOccurredAt(LocalDateTime.now());
    GroupActivity requested = new GroupActivity();
    requested.setActor(student);
    requested.setType(GroupActivityType.APPROVAL_REQUESTED);
    requested.setOccurredAt(LocalDateTime.of(2026, 10, 16, 9, 15));
    User addedStudent = new User();
    addedStudent.setFullName("Student Two");
    GroupActivity joined = new GroupActivity();
    joined.setActor(student);
    joined.setSubject(addedStudent);
    joined.setType(GroupActivityType.MEMBER_JOINED);
    joined.setOccurredAt(LocalDateTime.now());
    GroupActivity removed = new GroupActivity();
    removed.setActor(student);
    removed.setSubject(addedStudent);
    removed.setType(GroupActivityType.MEMBER_LEFT);
    removed.setOccurredAt(LocalDateTime.now());
    when(studentGroupService.findGroup(4L, 3L)).thenReturn(group);
    when(studentGroupService.canViewGroup(group, student)).thenReturn(true);
    when(studentGroupService.members(4L)).thenReturn(List.of(leader));
    when(studentGroupService.registration(4L)).thenReturn(registration);
    when(studentGroupService.isOpen(period)).thenReturn(true);
    when(activityService.history(4L)).thenReturn(List.of(requested, activity, joined, removed));

    mockMvc.perform(get("/dot/3/nhom/4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Network Analysis")))
        .andExpect(content().string(containsString("Trạng thái đăng ký: Chờ GVHD xét duyệt")))
        .andExpect(content().string(containsString("Gửi lúc: 16/10/2026 09:15")))
        .andExpect(content().string(containsString("Hạn duyệt đăng ký:")))
        .andExpect(content().string(containsString("Mã đề tài: #7")))
        .andExpect(content().string(containsString("Bộ môn: Computer Science")))
        .andExpect(content().string(containsString("lecturer@example.test")))
        .andExpect(content().string(containsString("Student One đã thêm Student Two vào nhóm.")))
        .andExpect(content().string(containsString("Student One đã xóa Student Two khỏi nhóm.")))
        .andExpect(content().string(containsString("name=\"studentCode\"")))
        .andExpect(content().string(containsString("Đăng ký được phê duyệt")));
  }

  @Test
  void groupDetailExplainsWhyApprovalIsUnavailableWithTwoMembers() throws Exception {
    Department department = new Department();
    department.setName("Computer Science");
    Topic topic = new Topic();
    topic.setTitle("Network Analysis");
    topic.setDepartment(department);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.DRAFT);
    GroupMember leader = new GroupMember();
    leader.setLeader(true);
    leader.setStudent(student);
    GroupMember member = new GroupMember();
    member.setStudent(new User());
    when(studentGroupService.findGroup(4L, 3L)).thenReturn(group);
    when(studentGroupService.canViewGroup(group, student)).thenReturn(true);
    when(studentGroupService.members(4L)).thenReturn(List.of(leader, member));
    when(studentGroupService.registration(4L)).thenReturn(registration);
    when(studentGroupService.isOpen(period)).thenReturn(true);

    mockMvc.perform(get("/dot/3/nhom/4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Trạng thái đăng ký: Chưa gửi phê duyệt")))
        .andExpect(content().string(containsString("Hạn đăng ký:")))
        .andExpect(content().string(containsString("Gửi yêu cầu phê duyệt")))
        .andExpect(content().string(containsString("Nhóm hiện có 2 thành viên")))
        .andExpect(content().string(containsString("disabled=\"disabled\"")));
  }

  @Test
  void groupDetailShowsApprovedReportDeadlineAndGuidance() throws Exception {
    TopicRegistration registration = registrationWithStatus(TopicRegistrationStatus.APPROVED);
    registration.setReviewedAt(LocalDateTime.of(2026, 10, 17, 10, 30));
    period.setReportDeadline(LocalDateTime.of(2026, 10, 31, 17, 0));
    stubGroupDetail(registration);

    mockMvc.perform(get("/dot/3/nhom/4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Trạng thái đăng ký: Đã phê duyệt")))
        .andExpect(content().string(containsString("Duyệt lúc: 17/10/2026 10:30")))
        .andExpect(content().string(containsString("Trưởng nhóm hãy nộp báo cáo")))
        .andExpect(content().string(containsString("Hạn nộp báo cáo:")))
        .andExpect(content().string(containsString("31/10/2026 17:00")));
  }

  @Test
  void groupDetailShowsRejectionReasonAndRegistrationDeadline() throws Exception {
    TopicRegistration registration = registrationWithStatus(TopicRegistrationStatus.REJECTED);
    registration.setReviewedAt(LocalDateTime.of(2026, 10, 17, 10, 30));
    registration.setRejectionReason("Cần sửa đề cương");
    stubGroupDetail(registration);

    mockMvc.perform(get("/dot/3/nhom/4"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Trạng thái đăng ký: Bị từ chối")))
        .andExpect(content().string(containsString("Từ chối lúc: 17/10/2026 10:30")))
        .andExpect(content().string(containsString("Cần sửa đề cương")))
        .andExpect(content().string(containsString("chọn lại đề tài")))
        .andExpect(content().string(containsString("Hạn đăng ký:")));
  }

  private TopicRegistration registrationWithStatus(TopicRegistrationStatus status) {
    Department department = new Department();
    department.setName("Computer Science");
    Topic topic = new Topic();
    topic.setTitle("Network Analysis");
    topic.setDepartment(department);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(status);
    return registration;
  }

  private void stubGroupDetail(TopicRegistration registration) {
    when(studentGroupService.findGroup(4L, 3L)).thenReturn(group);
    when(studentGroupService.canViewGroup(group, student)).thenReturn(true);
    when(studentGroupService.members(4L)).thenReturn(List.of());
    when(studentGroupService.registration(4L)).thenReturn(registration);
    when(studentGroupService.isOpen(period)).thenReturn(true);
    when(activityService.history(4L)).thenReturn(List.of());
  }

  @Test
  void studentPeriodsOfferCreateOrOpenBasedOnMembership() throws Exception {
    period.setStudentRegistrationStart(LocalDateTime.now().minusDays(1));
    RegistrationPeriod otherPeriod = new RegistrationPeriod();
    otherPeriod.setId(5L);
    otherPeriod.setName("Semester Two");
    otherPeriod.setStudentRegistrationStart(LocalDateTime.now().minusDays(1));
    otherPeriod.setStudentRegistrationEnd(LocalDateTime.now().plusDays(2));
    when(periodService.findVisibleForStudent(student.getId())).thenReturn(List.of(period, otherPeriod));
    when(studentGroupService.groupsForStudent(student)).thenReturn(List.of(group));

    mockMvc.perform(get("/dot"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("/dot/3/nhom/4")))
        .andExpect(content().string(containsString("/dot/5/nhom/tao")));
  }

  @Test
  void myGroupsIncludePastPeriods() throws Exception {
    Department department = new Department();
    department.setName("Computer Science");
    Topic topic = new Topic();
    topic.setTitle("Network Analysis");
    topic.setDepartment(department);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    when(studentGroupService.groupSummariesForStudent(student))
        .thenReturn(List.of(new GroupSummary(group, "Student One", "SV01", 3, List.of(student), registration)));
    when(studentGroupService.groupsForStudent(student)).thenReturn(List.of(group));
    when(periodService.findVisibleForStudent(student.getId())).thenReturn(List.of(period));

    mockMvc.perform(get("/dot").param("coNhom", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Four")))
        .andExpect(content().string(containsString("Semester One")))
        .andExpect(content().string(containsString("Network Analysis")))
        .andExpect(content().string(containsString("SV01 (Student One)")))
        .andExpect(content().string(containsString("Xem chi tiết nhóm")))
        .andExpect(content().string(not(containsString("Mở đợt"))));
  }

  @Test
  @WithMockUser(username = "student", roles = {"LECTURER", "STUDENT"})
  void groupFilterUsesStudentMembershipForMultipleRoles() throws Exception {
    when(periodService.findVisibleForStudent(student.getId())).thenReturn(List.of(period));
    when(studentGroupService.groupsForStudent(student)).thenReturn(List.of(group));
    when(studentGroupService.groupSummariesForStudent(student))
        .thenReturn(List.of(new GroupSummary(group, "Student One", "SV01", 1, List.of(student), null)));

    mockMvc.perform(get("/dot").param("coNhom", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Four")))
        .andExpect(content().string(containsString("/dot/3/nhom/4")));
  }

  @Test
  @WithMockUser(username = "student", roles = {"LECTURER", "STUDENT"})
  void periodGroupListIncludesOwnGroupForMultipleRoles() throws Exception {
    when(periodService.findById(3L)).thenReturn(period);
    when(periodService.findVisibleForLecturer(student.getId())).thenReturn(List.of(period));
    when(periodService.findVisibleForStudent(student.getId())).thenReturn(List.of(period));
    when(studentGroupService.groupSummariesForStudent(student))
        .thenReturn(List.of(new GroupSummary(group, "Student One", "SV01", 1, List.of(student), null)));

    mockMvc.perform(get("/dot/3/nhom"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Four")))
        .andExpect(content().string(containsString("/dot/3/nhom/4")));
  }

  @Test
  @WithMockUser(username = "student", roles = {"LECTURER", "STUDENT"})
  void pastStudentGroupListRemainsVisibleWithoutLecturerWork() throws Exception {
    when(periodService.findById(3L)).thenReturn(period);
    when(periodService.findVisibleForStudent(student.getId())).thenReturn(List.of(period));
    when(studentGroupService.groupSummariesForStudent(student))
        .thenReturn(List.of(new GroupSummary(group, "Student One", "SV01", 1, List.of(student), null)));

    mockMvc.perform(get("/dot/3/nhom"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Four")));
  }
}
