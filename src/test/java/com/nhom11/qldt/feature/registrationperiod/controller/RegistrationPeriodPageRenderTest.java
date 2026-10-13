package com.nhom11.qldt.feature.registrationperiod.controller;

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
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.council.model.dto.CouncilSummary;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.model.enums.RegistrationPeriodStage;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.studentgroup.model.dto.GroupSummary;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
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

  @Test
  void createPageRendersRegistrationPeriodFields() throws Exception {
    mockMvc.perform(get("/registration-periods/new"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"periodType\"")))
        .andExpect(content().string(containsString("data-date-picker")))
        .andExpect(content().string(not(containsString("topicPublicationAt"))));
  }

  @Test
  void editPageRendersRegistrationPeriodFields() throws Exception {
    stubPeriodPage();

    mockMvc.perform(get("/registration-periods/1/edit"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"periodType\"")))
        .andExpect(content().string(containsString("data-date-picker")))
        .andExpect(content().string(not(containsString("topicPublicationAt"))));
  }

  @Test
  void editPageDateFieldsCannotBeTypedInto() throws Exception {
    stubPeriodPage();

    String html = mockMvc.perform(get("/registration-periods/1/edit"))
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
  void detailPageStillRendersResultPublication() throws Exception {
    stubPeriodPage();
    when(councilService.publicationHistory(1L)).thenReturn(List.of());

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("revoke_publication")));
  }

  @Test
  void detailPageDisplaysDatesInDayMonthYearFormat() throws Exception {
    stubPeriodPage();

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("01/09/2026 08:30")));
  }

  @Test
  void detailPageShowsExistingCouncils() throws Exception {
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

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Council A")))
        .andExpect(content().string(containsString("Council B")))
        .andExpect(content().string(containsString("Chair A")))
        .andExpect(content().string(containsString("href=\"/registration-periods/1/councils/10\"")));
  }

  @Test
  void detailPageShowsGroupsWithoutTopicRegistration() throws Exception {
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
        new GroupSummary(group, "Student A", 2, null),
        new GroupSummary(registeredGroup, "Student B", 3, registration)));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Group Five")))
        .andExpect(content().string(containsString("Student A")))
        .andExpect(content().string(containsString("Selected topic")));
  }

  @Test
  void detailPageExpandsEvaluationStageAfterReportDeadline() throws Exception {
    stubPeriodPage();

    String html = mockMvc.perform(get("/registration-periods/1"))
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

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/registration-periods/1/topics/new\""))));
  }

  @Test
  void detailPageHidesEmptyTopicActionMenu() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(new PageImpl<>(List.of(topicIn(period))));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Test topic")))
        .andExpect(content().string(not(containsString("popovertarget=\"more_action_menu_7\""))));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminSeesTopicActionMenuOutsideProposalStage() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    when(topicService.canManageTopicsAsStaff(period)).thenReturn(true);
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(new PageImpl<>(List.of(topicIn(period))));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("popovertarget=\"more_action_menu_7\"")));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminCanAddTopicAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/registration-periods/1/topics/new\"")));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminCanAddTopicBeforeLecturerRegistrationStarts() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().plusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(2));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/registration-periods/1/topics/new\"")));
  }

  @Test
  void detailPageShowsAddTopicDuringLecturerRegistration() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/registration-periods/1/topics/new\"")));
  }

  @Test
  void editPageHidesAddTopicAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/registration-periods/1/edit"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/registration-periods/1/topics/new\""))));
  }

  @Test
  @WithMockUser(username = "lecturer", roles = "LECTURER")
  void lecturerDoesNotSeeAddTopicOnPeriodDetail() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationStart(LocalDateTime.now().minusDays(1));
    period.setLecturerRegistrationEnd(LocalDateTime.now().plusDays(1));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/registration-periods/1/topics/new\""))));
  }

  @Test
  void editAcceptsDayMonthYearDateTimeValues() throws Exception {
    stubPeriodPage();

    mockMvc.perform(post("/registration-periods/1")
            .with(csrf())
            .param("name", "2026-2027")
            .param("periodType", "MON_HOC")
            .param("lecturerRegistrationStart", "01/09/2026 08:30")
            .param("lecturerRegistrationEnd", "02/09/2026 08:30")
            .param("studentRegistrationStart", "03/09/2026 08:30")
            .param("studentRegistrationEnd", "04/09/2026 08:30")
            .param("reportDeadline", "05/09/2026 08:30"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/registration-periods/1"));

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
