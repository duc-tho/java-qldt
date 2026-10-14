package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topic-proposals")
public class TopicProposalController {
  private final RegistrationPeriodService registrationPeriodService;
  private final TopicService topicService;
  private final UserService userService;
  private final DepartmentService departmentService;
  private final MessageSource messageSource;

  public TopicProposalController(
      RegistrationPeriodService registrationPeriodService,
      TopicService topicService,
      UserService userService,
      DepartmentService departmentService,
      MessageSource messageSource) {
    this.registrationPeriodService = registrationPeriodService;
    this.topicService = topicService;
    this.userService = userService;
    this.departmentService = departmentService;
    this.messageSource = messageSource;
  }

  @GetMapping
  public String list(Model model) {
    LocalDateTime now = LocalDateTime.now();
    var periods = registrationPeriodService.findAll().stream()
        .filter(period -> !now.isBefore(period.getLecturerRegistrationStart()))
        .toList();
    var openPeriods = periods.stream()
        .filter(period -> !now.isAfter(period.getLecturerRegistrationEnd()))
        .sorted(Comparator.comparing(period -> period.getLecturerRegistrationEnd()))
        .toList();
    var closedPeriods = periods.stream()
        .filter(period -> now.isAfter(period.getLecturerRegistrationEnd()))
        .sorted(Comparator.comparing(RegistrationPeriod::getLecturerRegistrationEnd).reversed())
        .toList();
    var topicsByPeriodId = topicService.findProposalSummariesByPeriodIds(
        periods.stream().map(period -> period.getId()).toList()).stream()
        .collect(Collectors.groupingBy(TopicProposalSummary::periodId));
    model.addAttribute("openPeriods", openPeriods);
    model.addAttribute("closedPeriods", closedPeriods);
    model.addAttribute("topicsByPeriodId", topicsByPeriodId);
    model.addAttribute("pageTitle", "topics.page.propose.title");
    model.addAttribute("pageIcon", "bi:journal-plus");
    return "fragment/page/topic-proposal/periods";
  }

  @GetMapping("/{periodId}/topics")
  public String topics(
      @PathVariable Long periodId, @AuthenticationPrincipal UserDetails principal, Locale locale, Model model) {
    var period =
        registrationPeriodService.findAll().stream()
            .filter(value -> !LocalDateTime.now().isBefore(value.getLecturerRegistrationStart()))
            .filter(value -> value.getId().equals(periodId))
            .findFirst()
            .orElse(null);
    if (period == null) {
      return "redirect:/topic-proposals";
    }

    boolean isProposalOpen = topicService.isProposalWindowOpen(period);
    model.addAttribute("period", period);
    model.addAttribute("topics", topicService.findAllWithDetailsByRegistrationPeriodId(periodId));
    model.addAttribute("currentUserId", principal == null ? null : currentUser(principal).getId());
    model.addAttribute("isProposalOpen", isProposalOpen);
    model.addAttribute("pageTitle", "topics.page.lecturer-list.title");
    model.addAttribute("pageHeading", messageSource.getMessage(
        "topics.page.lecturer-list.heading", new Object[] {period.getName()}, locale));
    model.addAttribute("pageIcon", "bi:journal-text");
    return "fragment/page/topic-proposal/topics";
  }

  @GetMapping("/{periodId}/topics/{topicId}/edit")
  public String editForm(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) return "redirect:/topic-proposals/" + periodId + "/topics";
    User proposer = currentUser(principal);
    Topic topic = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!isProposalOwner(topic, proposer)) return "redirect:/topic-proposals/" + periodId + "/topics";
    if (!topicService.isProposalWindowOpen(topic.getRegistrationPeriod())) return "redirect:/topic-proposals/" + periodId + "/topics";
    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, periodId, topicId, form, proposer);
    return "fragment/page/topic-proposal/edit";
  }

  @PostMapping("/{periodId}/topics/{topicId}")
  public String update(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) {
    if (principal == null) return "redirect:/topic-proposals/" + periodId + "/topics";
    User proposer = currentUser(principal);
    Topic existing = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!isProposalOwner(existing, proposer)
        || !topicService.isProposalWindowOpen(existing.getRegistrationPeriod())) {
      return "redirect:/topic-proposals/" + periodId + "/topics";
    }
    if (errors.hasErrors()) {
      prepare(model, periodId, topicId, form, proposer);
      return "fragment/page/topic-proposal/edit";
    }
    try {
      topicService.updateProposal(topicId, periodId, proposer, form.toEntity(), form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepare(model, periodId, topicId, form, proposer);
      return "fragment/page/topic-proposal/edit";
    }
    return "redirect:/topic-proposals/" + periodId + "/topics";
  }

  @PostMapping("/{periodId}/topics/{topicId}/delete")
  public String delete(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    if (principal == null) return "redirect:/login";
    try {
      topicService.deleteProposal(topicId, periodId, currentUser(principal));
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("deleteError", exception.getMessage());
    } catch (DataIntegrityViolationException exception) {
      attributes.addFlashAttribute("deleteError", "crud.delete.error.in-use");
    }
    return "redirect:/topic-proposals/" + periodId + "/topics";
  }

  private void prepare(Model model, Long periodId, Long topicId, TopicForm form, User proposer) {
    if (form.getAdviserIds() == null) form.setAdviserIds(new LinkedHashSet<>());
    form.getAdviserIds().add(proposer.getId());
    model.addAttribute("data", form);
    model.addAttribute("periodId", periodId);
    model.addAttribute("topicId", topicId);
    model.addAttribute("departments", departmentService.findAll());
    model.addAttribute("selectedLecturers", userService.findAllLecturersByIds(form.getAdviserIds()));
    model.addAttribute("requiredAdviserId", proposer.getId());
    model.addAttribute("lecturerSearchUrl", "/registration-periods/" + periodId + "/topics/lecturers");
    model.addAttribute("pageTitle", "topics.page.edit.title");
  }
  private User currentUser(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
  private boolean isProposalOwner(Topic topic, User user) { return topic != null && (topic.getStatus() == TopicStatus.PROPOSED || topic.getStatus() == TopicStatus.REJECTED) && topic.getProposedBy() != null && topic.getProposedBy().getId().equals(user.getId()); }
}
