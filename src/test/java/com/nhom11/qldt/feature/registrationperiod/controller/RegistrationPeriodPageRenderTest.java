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
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
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
  void detailPageHidesAddTopicAfterLecturerRegistrationEnds() throws Exception {
    RegistrationPeriod period = stubPeriodPage();
    period.setLecturerRegistrationEnd(LocalDateTime.now().minusDays(1));

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("href=\"/registration-periods/1/topics/new\""))));
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
    when(periodService.findById(1L)).thenReturn(period);
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(Page.empty());
    when(studentGroupService.registrations(1L)).thenReturn(List.of());
    return period;
  }
}
