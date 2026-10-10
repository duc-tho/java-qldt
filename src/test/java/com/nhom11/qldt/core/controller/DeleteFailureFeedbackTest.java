package com.nhom11.qldt.core.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.department.view.DepartmentTableDefinition;
import com.nhom11.qldt.core.table.TableQuery;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class DeleteFailureFeedbackTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private DepartmentService service;
  @MockitoBean private UserService userService;
  @MockitoBean private RegistrationPeriodService periodService;
  @MockitoBean private TopicService topicService;
  @MockitoBean private CouncilService councilService;
  @MockitoBean private StudentGroupService studentGroupService;

  @Test
  void referencedDepartmentShowsReasonInsteadOfFailingSilently() throws Exception {
    doThrow(new IllegalStateException("Department has topics")).when(service).delete(1L);
    when(service.findAll(any(TableQuery.class), any(DepartmentTableDefinition.class)))
        .thenReturn(Page.empty());

    mockMvc.perform(post("/departments/1/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/departments"))
        .andExpect(flash().attribute("deleteError", "departments.error.has-topics"));

    mockMvc.perform(get("/departments")
            .flashAttr("deleteError", "departments.error.has-topics"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Bộ môn này đang có đề tài")));
  }

  @Test
  void englishLocaleRendersBothDeleteErrors() throws Exception {
    when(service.findAll(any(TableQuery.class), any(DepartmentTableDefinition.class)))
        .thenReturn(Page.empty());

    mockMvc.perform(get("/departments").param("lang", "en")
            .flashAttr("deleteError", "departments.error.has-topics"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("This department has topics and cannot be deleted.")));

    mockMvc.perform(get("/departments").param("lang", "en")
            .flashAttr("deleteError", "crud.delete.error.in-use"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("This record is in use and cannot be deleted.")));
  }

  @Test
  void databaseReferenceFailureShowsGenericDeleteReason() throws Exception {
    doThrow(new DataIntegrityViolationException("foreign key")).when(service).delete(2L);

    mockMvc.perform(post("/departments/2/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }

  @Test
  void referencedUserCannotBePermanentlyDeletedWithoutFeedback() throws Exception {
    doThrow(new DataIntegrityViolationException("foreign key"))
        .when(userService).deletePermanently(3L);

    mockMvc.perform(post("/users/3/delete").param("force", "1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/users"))
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }

  @Test
  void periodDeleteReferenceFailureShowsReason() throws Exception {
    doThrow(new DataIntegrityViolationException("foreign key")).when(periodService).delete(4L);

    mockMvc.perform(post("/registration-periods/4/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }

  @Test
  void topicDeleteReferenceFailureShowsReason() throws Exception {
    when(periodService.findById(4L)).thenReturn(new RegistrationPeriod());
    when(topicService.findByIdAndRegistrationPeriodId(5L, 4L)).thenReturn(new Topic());
    doThrow(new DataIntegrityViolationException("foreign key")).when(topicService).delete(5L);

    mockMvc.perform(post("/registration-periods/4/topics/5/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }

  @Test
  @WithMockUser(username = "user", roles = "LECTURER")
  void proposalDeleteBusinessFailureShowsReason() throws Exception {
    User proposer = new User();
    when(userService.findByEmail("user")).thenReturn(Optional.of(proposer));
    doThrow(new IllegalArgumentException("topics.error.proposal-owner-required"))
        .when(topicService).deleteProposal(5L, 4L, proposer);

    mockMvc.perform(post("/topic-proposals/4/topics/5/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "topics.error.proposal-owner-required"));
  }

  @Test
  void councilDeleteReferenceFailureShowsReason() throws Exception {
    doThrow(new DataIntegrityViolationException("foreign key"))
        .when(councilService).deleteCouncil(4L, 6L);

    mockMvc.perform(post("/registration-periods/4/councils/6/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }

  @Test
  @WithMockUser(username = "user", roles = "STUDENT")
  void memberDeleteReferenceFailureShowsReason() throws Exception {
    User student = new User();
    when(userService.findByEmail("user")).thenReturn(Optional.of(student));
    doThrow(new DataIntegrityViolationException("foreign key"))
        .when(studentGroupService).removeMember(7L, student, 8L);

    mockMvc.perform(post("/registration-periods/4/student-group/7/members/8/delete").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("deleteError", "crud.delete.error.in-use"));
  }
}
