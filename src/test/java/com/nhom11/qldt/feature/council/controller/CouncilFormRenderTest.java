package com.nhom11.qldt.feature.council.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
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
class CouncilFormRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private CouncilService councilService;

  @Test
  @WithMockUser(roles = "ADMIN")
  void formRendersMemberPickerAndDistinctRoleSelects() throws Exception {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Council period");
    period.setPeriodType(PeriodType.KLTN);
    when(periodService.findById(1L)).thenReturn(period);
    when(councilService.isCouncilManagementOpen(1L)).thenReturn(true);
    when(councilService.lecturerOptions(null)).thenReturn(List.of());

    mockMvc.perform(get("/dot/1/hoi-dong/tao"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"chairpersonId\"")))
        .andExpect(content().string(containsString("id=\"secretaryId\"")))
        .andExpect(content().string(containsString("x-model=\"chairperson\"")))
        .andExpect(content().string(containsString("x-model=\"secretary\"")))
        .andExpect(content().string(containsString("add(lecturer)")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void detailRendersBothAssignmentTablesWithSharedCells() throws Exception {
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Council period");
    Council council = new Council();
    council.setId(2L);
    council.setName("Council One");
    council.setRegistrationPeriod(period);
    Topic topic = new Topic();
    topic.setId(3L);
    topic.setTitle("Example topic");
    StudentGroup group = new StudentGroup();
    group.setId(4L);
    group.setName("Example group");
    group.setRegistrationPeriod(period);
    TopicRegistration registration = new TopicRegistration();
    registration.setTopic(topic);
    registration.setStudentGroup(group);
    when(councilService.findCouncil(1L, 2L)).thenReturn(council);
    when(councilService.members(2L)).thenReturn(List.of());
    when(councilService.eligibleUnassignedRegistrations(2L)).thenReturn(List.of(registration));
    when(councilService.assignedRegistrations(2L)).thenReturn(List.of());
    when(councilService.isCouncilManagementOpen(1L)).thenReturn(true);

    mockMvc.perform(get("/dot/1/hoi-dong/2"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Council One")))
        .andExpect(content().string(containsString("Example topic")))
        .andExpect(content().string(containsString("Example group")))
        .andExpect(content().string(containsString("/hoi-dong/2/de-tai/3")));
  }
}
