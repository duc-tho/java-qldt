package com.nhom11.qldt.feature.registrationperiod.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import com.nhom11.qldt.feature.topic.view.TopicTableDefinition;
import java.util.List;
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
        .andExpect(content().string(containsString("x-effect=")));
  }

  @Test
  void editPageRendersRegistrationPeriodFields() throws Exception {
    stubPeriodPage();

    mockMvc.perform(get("/registration-periods/1/edit"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"periodType\"")))
        .andExpect(content().string(containsString("x-effect=")));
  }

  @Test
  void detailPageStillRendersResultPublication() throws Exception {
    stubPeriodPage();
    when(councilService.publicationHistory(1L)).thenReturn(List.of());

    mockMvc.perform(get("/registration-periods/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("revoke_publication")));
  }

  private void stubPeriodPage() {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("2025-2026");
    period.setPeriodType(PeriodType.MON_HOC);
    when(periodService.findById(1L)).thenReturn(period);
    when(topicService.findAllByRegistrationPeriodId(
            eq(1L), any(TableQuery.class), any(TopicTableDefinition.class)))
        .thenReturn(Page.empty());
    when(studentGroupService.registrations(1L)).thenReturn(List.of());
  }
}
