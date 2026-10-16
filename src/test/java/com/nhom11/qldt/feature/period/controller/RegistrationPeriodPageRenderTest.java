package com.nhom11.qldt.feature.period.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.model.dto.CouncilSummary;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "template-test", roles = "HEAD_OF_FACULTY")
class RegistrationPeriodPageRenderTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private TopicService topicService;
  @MockitoBean private StudentGroupService studentGroupService;
  @MockitoBean private CouncilService councilService;
  @MockitoBean private UserService userService;

  @Test
  @WithMockUser(username = "student@example.com", roles = "STUDENT")
  void studentPeriodListUsesStudentVisibilityAndExistingCard() throws Exception {
    User student = new User();
    student.setId(7L);
    when(userService.findByEmail("student@example.com")).thenReturn(Optional.of(student));
    RegistrationPeriod visible = new RegistrationPeriod();
    visible.setId(1L);
    visible.setName("Visible period");
    visible.setStudentRegistrationStart(LocalDateTime.of(2026, 10, 1, 12, 0));
    visible.setStudentRegistrationEnd(LocalDateTime.of(2026, 11, 1, 12, 0));
    when(periodService.findVisibleForStudent(7L)).thenReturn(List.of(visible));
    when(studentGroupService.groupsForStudent(student)).thenReturn(List.of());

    mockMvc.perform(get("/dot"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Visible period")))
        .andExpect(content().string(containsString("/dot/1")));

    verify(periodService).findVisibleForStudent(7L);
  }

  @Test
  @WithMockUser(username = "lecturer@example.com", roles = "LECTURER")
  void lecturerPeriodListUsesVisibleProposalSummaries() throws Exception {
    User lecturer = new User();
    lecturer.setId(9L);
    when(userService.findByEmail("lecturer@example.com")).thenReturn(Optional.of(lecturer));
    RegistrationPeriod visible = new RegistrationPeriod();
    visible.setId(2L);
    visible.setName("Lecturer period");
    visible.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    visible.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));
    when(periodService.findVisibleForLecturer(9L)).thenReturn(List.of(visible));
    when(topicService.findVisibleProposalSummariesByPeriodIds(List.of(2L), 9L))
        .thenReturn(List.of(new TopicProposalSummary(3L, 2L, "Visible topic", TopicStatus.APPROVED)));

    mockMvc.perform(get("/dot"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Lecturer period")))
        .andExpect(content().string(containsString("Visible topic")))
        .andExpect(content().string(containsString("/dot/2/de-tai")));

    verify(periodService).findVisibleForLecturer(9L);
  }

  @Test
  @WithMockUser(username = "lecturer@example.com", roles = "LECTURER")
  void pendingWorkPeriodOpensGroupReview() throws Exception {
    User lecturer = new User();
    lecturer.setId(9L);
    when(userService.findByEmail("lecturer@example.com")).thenReturn(Optional.of(lecturer));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(2L);
    period.setName("Review period");
    when(periodService.findVisibleForLecturer(9L)).thenReturn(List.of(period));
    Topic topic = new Topic();
    topic.setRegistrationPeriod(period);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.PENDING);
    when(studentGroupService.adviserRegistrations(lecturer)).thenReturn(List.of(registration));

    mockMvc.perform(get("/dot").param("congViec", "duyet"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Review period")))
        .andExpect(content().string(containsString("/dot/2/nhom?canDuyet=true")))
        .andExpect(content().string(not(containsString("/dot/2/de-tai/tao"))));
  }

  @Test
  @WithMockUser(username = "lecturer@example.com", roles = "LECTURER")
  void evaluationWorkPeriodOpensAssignedEvaluations() throws Exception {
    User lecturer = new User();
    lecturer.setId(9L);
    when(userService.findByEmail("lecturer@example.com")).thenReturn(Optional.of(lecturer));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(2L);
    period.setName("Evaluation period");
    when(periodService.findVisibleForLecturer(9L)).thenReturn(List.of(period));
    Topic topic = new Topic();
    topic.setRegistrationPeriod(period);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    when(councilService.workspaceRegistrations(lecturer)).thenReturn(List.of(registration));

    mockMvc.perform(get("/dot").param("congViec", "danh-gia"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Evaluation period")))
        .andExpect(content().string(containsString("/dot/2/danh-gia")))
        .andExpect(content().string(not(containsString("/dot/2/de-tai/tao"))));
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = "STUDENT")
  void studentCannotOpenPeriodOutsideVisibleScope() throws Exception {
    User student = new User();
    student.setId(7L);
    when(userService.findByEmail("student@example.com")).thenReturn(Optional.of(student));

    mockMvc.perform(get("/dot/1")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = {"LECTURER", "STUDENT"})
  void multipleRolesCanOpenPastStudentPeriodWithoutLecturerWork() throws Exception {
    User viewer = new User();
    viewer.setId(7L);
    when(userService.findByEmail("student@example.com")).thenReturn(Optional.of(viewer));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Past student period");
    period.setPeriodType(PeriodType.TLCN);
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(30));
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(25));
    period.setStudentRegistrationStart(LocalDateTime.now().minusDays(20));
    period.setStudentRegistrationEnd(LocalDateTime.now().minusDays(15));
    period.setReportDeadline(LocalDateTime.now().minusDays(5));
    when(periodService.findVisibleForStudent(7L)).thenReturn(List.of(period));
    when(periodService.stage(eq(period), any())).thenReturn(RegistrationPeriodStage.CLOSED);
    StudentGroup group = new StudentGroup();
    group.setId(4L);
    group.setRegistrationPeriod(period);
    when(studentGroupService.groupsForStudent(viewer)).thenReturn(List.of(group));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Past student period")))
        .andExpect(content().string(containsString("/dot/1/nhom/4")));
  }

  @Test
  @WithMockUser(username = "student@example.com", roles = "STUDENT")
  void studentPeriodDetailShowsOverviewWithoutStaffData() throws Exception {
    User student = new User();
    student.setId(7L);
    when(userService.findByEmail("student@example.com")).thenReturn(Optional.of(student));
    RegistrationPeriod visible = new RegistrationPeriod();
    visible.setId(1L);
    visible.setName("Student period");
    visible.setPeriodType(PeriodType.TLCN);
    visible.setLecturerRegistrationStart(LocalDateTime.now().minusDays(10));
    visible.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(8));
    visible.setStudentRegistrationStart(LocalDateTime.now().minusDays(1));
    visible.setStudentRegistrationEnd(LocalDateTime.now().plusDays(1));
    visible.setReportDeadline(LocalDateTime.now().plusDays(20));
    when(periodService.findVisibleForStudent(7L)).thenReturn(List.of(visible));
    when(periodService.stage(eq(visible), any())).thenReturn(RegistrationPeriodStage.TOPIC_EXECUTION);
    when(studentGroupService.groupsForStudent(student)).thenReturn(List.of());

    String html = mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Student period")))
        .andExpect(content().string(not(containsString("revoke_publication"))))
        .andExpect(content().string(not(containsString("Council A"))))
        .andReturn().getResponse().getContentAsString();

    assertEquals(1, html.split("href=\"/dot/1/de-tai\"", -1).length - 1);
  }

  @Test
  void staffPeriodDetailUsesExistingOverview() throws Exception {
    stubPeriodPage();
    when(topicService.countByPeriod(1L)).thenReturn(2L);
    when(studentGroupService.countByPeriod(1L)).thenReturn(3L);

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("01/09/2026 08:30")))
        .andExpect(content().string(containsString("Danh sách đề tài: 2")))
        .andExpect(content().string(containsString("Danh sách nhóm đăng ký: 3")));
  }

  @Test
  void createPageRendersRegistrationPeriodFields() throws Exception {
    mockMvc.perform(get("/dot/tao"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"periodType\"")))
        .andExpect(content().string(containsString("data-date-picker")))
        .andExpect(content().string(not(containsString("topicPublicationAt"))));
  }

  @Test
  void canonicalCreateRouteKeepsTheCanonicalFormAction() throws Exception {
    mockMvc.perform(get("/dot/tao"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("action=\"/dot\"")));

    mockMvc.perform(post("/dot").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("action=\"/dot\"")));
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotPostPeriodChanges() throws Exception {
    mockMvc.perform(post("/dot").with(csrf())).andExpect(status().isForbidden());
    mockMvc.perform(post("/dot/1/xoa").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  void editPageRendersRegistrationPeriodFields() throws Exception {
    stubPeriodPage();

    mockMvc.perform(get("/dot/1/sua"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"periodType\"")))
        .andExpect(content().string(containsString("data-required-for=\"periodType\"")))
        .andExpect(content().string(containsString("data-help-for=\"lecturerRegistrationStart\"")))
        .andExpect(content().string(containsString("data-required-for=\"reviewerDeadline\"")))
        .andExpect(content().string(containsString("data-required-for=\"councilDate\"")))
        .andExpect(content().string(containsString("data-date-picker")))
        .andExpect(content().string(not(containsString("topicPublicationAt"))));
  }

  @Test
  void editPageDateFieldsCannotBeTypedInto() throws Exception {
    stubPeriodPage();

    String html = mockMvc.perform(get("/dot/1/sua"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    Matcher fields = Pattern.compile("<input[^>]*data-date-picker[^>]*>", Pattern.DOTALL)
        .matcher(html);
    int count = 0;
    while (fields.find()) {
      assertTrue(fields.group().contains("readonly"));
      assertTrue(fields.group().contains("data-date-picker-mode=\"edit\""));
      count++;
    }
    assertEquals(7, count);
  }

  @Test
  void detailPageLinksToResultPublication() throws Exception {
    stubPeriodPage();
    when(councilService.publicationHistory(1L)).thenReturn(List.of());

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/ket-qua\"")))
        .andExpect(content().string(not(containsString("revoke_publication"))));
  }

  @Test
  void detailPageDisplaysDatesInDayMonthYearFormat() throws Exception {
    stubPeriodPage();

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("01/09/2026 08:30")));
  }

  @Test
  void detailPageLinksToCouncilListWithoutRenderingCouncilRows() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setPeriodType(PeriodType.KLTN);
    Council first = new Council();
    first.setId(10L);
    first.setName("Council A");
    Council second = new Council();
    second.setId(11L);
    second.setName("Council B");
    when(councilService.councils(1L)).thenReturn(List.of(first, second));
    when(councilService.councilSummaries(eq(1L), any())).thenReturn(List.of(
        new CouncilSummary(first, "Chair A", 3, 2),
        new CouncilSummary(second, "Chair B", 4, 1)));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/hoi-dong\"")))
        .andExpect(content().string(not(containsString("Council A"))));
  }

  @Test
  void detailPageLinksToGroupListWithoutRenderingGroupRows() throws Exception {
    stubPeriodPage();
    StudentGroup group = new StudentGroup();
    group.setId(5L);
    group.setName("Group Five");
    StudentGroup registeredGroup = new StudentGroup();
    registeredGroup.setId(6L);
    registeredGroup.setName("Group Six");
    Topic topic = new Topic();
    topic.setTitle("Selected topic");
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStatus(TopicRegistrationStatus.APPROVED);
    when(studentGroupService.groupSummaries(1L)).thenReturn(List.of(
        new GroupSummary(group, "Student A", "SVA", 2, List.of(), null),
        new GroupSummary(registeredGroup, "Student B", "SVB", 3, List.of(), registration)));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/nhom\"")))
        .andExpect(content().string(not(containsString("Group Five"))));
  }

  @Test
  void detailPageExpandsEvaluationStageAfterReportDeadline() throws Exception {
    stubPeriodPage();

    String html = mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

    Matcher expandedStages = Pattern.compile("<details[^>]*open=\"open\"[^>]*>", Pattern.DOTALL)
        .matcher(html);
    int count = 0;
    while (expandedStages.find()) count++;

    assertEquals(1, count);
    assertTrue(html.contains("Đánh giá và công bố"));
    assertTrue(html.contains("Kết thúc đợt"));
    assertTrue(html.contains("Công bố kết quả để sinh viên tra cứu và kết thúc đợt đăng ký."));
  }

  @Test
  void detailPageHidesAddTopicAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/dot/1/de-tai/tao\""))));
  }

  @Test
  void detailPageLinksToTopicListWithoutEmbeddingTopicRows() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(new PageImpl<>(List.of(topicIn(period))));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/de-tai\"")))
        .andExpect(content().string(not(containsString("Test topic"))))
        .andExpect(content().string(not(containsString("popovertarget=\"more_action_menu_7\""))));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminDoesNotSeeTopicActionMenuOnPeriodSummary() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    when(topicService.canManageTopicsAsStaff(period)).thenReturn(true);
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(new PageImpl<>(List.of(topicIn(period))));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("popovertarget=\"more_action_menu_7\""))));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminCanOpenTopicListAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/de-tai\"")));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminCanOpenTopicListBeforeLecturerRegistrationStarts() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().plusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(2));

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/de-tai\"")));
  }

  @Test
  void detailPageLinksToTopicListDuringLecturerRegistration() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));
    when(topicService.isProposalWindowOpen(period)).thenReturn(true);

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/de-tai\"")));
  }

  @Test
  void editPageHidesAddTopicAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/dot/1/sua"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/dot/1/de-tai/tao\""))));
  }

  @Test
  @WithMockUser(username = "lecturer", roles = "LECTURER")
  void lecturerSeesProposeTopicDuringProposalWindow() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));
    User lecturer = new User();
    lecturer.setId(9L);
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    when(periodService.findVisibleForLecturer(9L)).thenReturn(List.of(period));
    when(periodService.stage(eq(period), any())).thenReturn(RegistrationPeriodStage.TOPIC_PROPOSAL);

    mockMvc.perform(get("/dot/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/dot/1/de-tai/tao\"")));
  }

  @Test
  @WithMockUser(username = "lecturer", roles = "LECTURER")
  void lecturerPeriodCardRendersItsOwnTopicData() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));
    User lecturer = new User();
    lecturer.setId(9L);
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    when(periodService.findVisibleForLecturer(9L)).thenReturn(List.of(period));
    when(topicService.findVisibleProposalSummariesByPeriodIds(List.of(1L), 9L))
        .thenReturn(List.of(new TopicProposalSummary(10L, 1L, "Sample topic", TopicStatus.PROPOSED)));

    mockMvc.perform(get("/dot"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Sample topic")))
        .andExpect(content().string(containsString("2025-2026")));
  }

  @Test
  void editAcceptsDayMonthYearDateTimeValues() throws Exception {
    stubPeriodPage();

    mockMvc.perform(post("/dot/1")
            .with(csrf())
            .param("name", "2026-2027")
            .param("periodType", "MON_HOC")
            .param("lecturerRegistrationStart", "01/09/2026 08:30")
            .param("lecturerRegistrationEnd", "02/09/2026 08:30")
            .param("studentRegistrationStart", "03/09/2026 08:30")
            .param("studentRegistrationEnd", "04/09/2026 08:30")
            .param("reportDeadline", "05/09/2026 08:30"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dot/1"));

    verify(periodService).update(argThat(period ->
        LocalDateTime.of(2026, 9, 1, 8, 30).equals(period.getLecturerRegistrationStart())));
  }

  private RegistrationPeriod stubPeriodPage() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("2025-2026");
    period.setPeriodType(PeriodType.MON_HOC);
    period.setLecturerRegistrationStart(LocalDateTime.of(2026, 9, 1, 8, 30));
    period.setLecturerRegistrationEnd(LocalDateTime.of(2026, 9, 2, 8, 30));
    period.setStudentRegistrationStart(LocalDateTime.of(2026, 9, 3, 8, 30));
    period.setStudentRegistrationEnd(LocalDateTime.of(2026, 9, 4, 8, 30));
    period.setReportDeadline(LocalDateTime.of(2026, 9, 5, 8, 30));
    when(periodService.findById(1L)).thenReturn(period);
    when(periodService.stage(eq(period), any(LocalDateTime.class)))
        .thenReturn(RegistrationPeriodStage.CLOSED);
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(Page.empty());
    when(studentGroupService.registrations(1L)).thenReturn(List.of());
    return period;
  }

  private Topic topicIn(RegistrationPeriod period) {
    Department department = new Department();
    department.setName("Computer science");
    Topic topic = new Topic();
    topic.setId(7L);
    topic.setTitle("Test topic");
    topic.setDepartment(department);
    topic.setRegistrationPeriod(period);
    topic.setStatus(TopicStatus.PROPOSED);
    return topic;
  }
}
