package com.nhom11.qldt.feature.council.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
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
class EvaluationDetailRenderTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private CouncilService councilService;
  @MockitoBean private ReportSubmissionService reportService;
  @MockitoBean private UserService userService;

  @Test
  @WithMockUser(username = "lecturer", roles = "LECTURER")
  void detailKeepsScoreActionsAndReportContext() throws Exception {
    User lecturer = new User();
    lecturer.setId(7L);
    when(userService.findByEmail("lecturer")).thenReturn(Optional.of(lecturer));
    RegistrationPeriod period = new RegistrationPeriod();
    period.setId(1L);
    period.setName("Period One");
    period.setPeriodType(PeriodType.TLCN);
    Topic topic = new Topic();
    topic.setTitle("Example topic");
    topic.setRegistrationPeriod(period);
    StudentGroup group = new StudentGroup();
    group.setId(2L);
    group.setName("Example group");
    group.setRegistrationPeriod(period);
    TopicRegistration registration = new TopicRegistration();
    registration.setId(3L);
    registration.setTopic(topic);
    registration.setStudentGroup(group);
    when(councilService.workspaceRegistration(3L, lecturer)).thenReturn(registration);
    when(councilService.evaluations(3L)).thenReturn(List.of());
    when(councilService.canEvaluate(3L, lecturer)).thenReturn(true);

    mockMvc.perform(get("/dot/1/danh-gia/3"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Period One")))
        .andExpect(content().string(containsString("Example topic")))
        .andExpect(content().string(containsString("id=\"score\"")))
        .andExpect(content().string(containsString("id=\"comment\"")))
        .andExpect(content().string(containsString("/dot/1/danh-gia/3/luu-nhap")))
        .andExpect(content().string(containsString("/dot/1/danh-gia/3/nop")));
  }
}
