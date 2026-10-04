package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/topic-proposals")
public class TopicProposalController {
  private final RegistrationPeriodService registrationPeriodService;
  private final TopicService topicService;
  private final UserService userService;
  private final DepartmentService departmentService;

  public TopicProposalController(
      RegistrationPeriodService registrationPeriodService,
      TopicService topicService,
      UserService userService,
      DepartmentService departmentService) {
    this.registrationPeriodService = registrationPeriodService;
    this.topicService = topicService;
    this.userService = userService;
    this.departmentService = departmentService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("periods", registrationPeriodService.findVisibleForLecturers());
    model.addAttribute("now", LocalDateTime.now());
    model.addAttribute("pageTitle", "topics.page.propose.title");
    model.addAttribute("pageIcon", "bi:journal-plus");
    return "fragment/page/topic-proposal/periods";
  }

  @GetMapping("/{periodId}/topics")
  public String topics(
      @PathVariable Long periodId, @AuthenticationPrincipal UserDetails principal, Model model) {
    var period =
        registrationPeriodService.findVisibleForLecturers().stream()
            .filter(value -> value.getId().equals(periodId))
            .findFirst()
            .orElse(null);
    if (period == null) {
      return "redirect:/topic-proposals";
    }

    LocalDateTime now = LocalDateTime.now();
    boolean isProposalOpen =
        !now.isBefore(period.getLecturerRegistrationStart())
            && !now.isAfter(period.getLecturerRegistrationEnd());
    model.addAttribute("period", period);
    model.addAttribute("topics", topicService.findAllWithDetailsByRegistrationPeriodId(periodId));
    model.addAttribute("currentUserId", principal == null ? null : currentUser(principal).getId());
    model.addAttribute("isProposalOpen", isProposalOpen);
    model.addAttribute("pageTitle", "topics.page.lecturer-list.title");
    model.addAttribute("pageIcon", "bi:journal-text");
    return "fragment/page/topic-proposal/topics";
  }

  @GetMapping("/{periodId}/topics/{topicId}/edit")
  public String editForm(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, Model model) {
    Topic topic = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (principal == null || !isProposalOwner(topic, currentUser(principal))) return "redirect:/topic-proposals/" + periodId + "/topics";
    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, periodId, topicId, form);
    return "fragment/page/topic-proposal/edit";
  }

  @PostMapping("/{periodId}/topics/{topicId}")
  public String update(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) {
    if (principal == null) return "redirect:/login";
    if (errors.hasErrors()) { prepare(model, periodId, topicId, form); return "fragment/page/topic-proposal/edit"; }
    try { topicService.updateProposal(topicId, periodId, currentUser(principal), form.toEntity(), form.getDepartmentId(), form.getAdviserIds()); }
    catch (IllegalArgumentException exception) { errors.reject(exception.getMessage()); prepare(model, periodId, topicId, form); return "fragment/page/topic-proposal/edit"; }
    return "redirect:/topic-proposals/" + periodId + "/topics";
  }

  @PostMapping("/{periodId}/topics/{topicId}/delete")
  public String delete(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal) {
    if (principal != null) { try { topicService.deleteProposal(topicId, periodId, currentUser(principal)); } catch (IllegalArgumentException ignored) { } }
    return "redirect:/topic-proposals/" + periodId + "/topics";
  }

  private void prepare(Model model, Long periodId, Long topicId, TopicForm form) { model.addAttribute("data", form); model.addAttribute("periodId", periodId); model.addAttribute("topicId", topicId); model.addAttribute("departments", departmentService.findAll()); model.addAttribute("lecturers", userService.findAllLecturers()); model.addAttribute("pageTitle", "topics.page.edit.title"); }
  private User currentUser(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
  private boolean isProposalOwner(Topic topic, User user) { return topic != null && (topic.getStatus() == TopicStatus.PROPOSED || topic.getStatus() == TopicStatus.REJECTED) && topic.getProposedBy() != null && topic.getProposedBy().getId().equals(user.getId()); }
}
