package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TopicProposalController {
  private final TopicService topicService;
  private final UserService userService;
  private final DepartmentService departmentService;
  private final CrudViewSupport crudView;
  private final ClientValidationRuleRenderer validation;
  private final RegistrationPeriodService periodService;

  public TopicProposalController(
      TopicService topicService,
      UserService userService,
      DepartmentService departmentService,
      CrudViewSupport crudView,
      ClientValidationRuleRenderer validation,
      RegistrationPeriodService periodService) {
    this.topicService = topicService;
    this.userService = userService;
    this.departmentService = departmentService;
    this.crudView = crudView;
    this.validation = validation;
    this.periodService = periodService;
  }

  @GetMapping("/de-xuat-de-tai")
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User lecturer = currentUser(principal);
    List<RegistrationPeriod> periods = periodService.findVisibleForLecturer(lecturer.getId());
    Map<Long, List<TopicProposalSummary>> topicsByPeriodId = topicService
        .findProposalSummariesByPeriodIdsAndProposedById(
            periods.stream().map(RegistrationPeriod::getId).toList(), lecturer.getId())
        .stream()
        .collect(Collectors.groupingBy(TopicProposalSummary::periodId));
    List<RegistrationPeriod> openPeriods = periods.stream()
        .filter(topicService::isProposalWindowOpen)
        .toList();
    List<RegistrationPeriod> closedPeriods = periods.stream()
        .filter(period -> !topicService.isProposalWindowOpen(period))
        .toList();

    model.addAttribute("openPeriods", openPeriods);
    model.addAttribute("closedPeriods", closedPeriods);
    model.addAttribute("topicsByPeriodId", topicsByPeriodId);
    model.addAttribute("pageTitle", "navigation.item.topic-proposals");
    model.addAttribute("pageDesc", "work.proposal.description");
    model.addAttribute("pageIcon", "bi:journal-plus");
    return "page/topic/proposals";
  }

  public String editForm(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) return "redirect:/dot/" + periodId + "/de-tai";
    User proposer = currentUser(principal);
    Topic topic = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!topicService.canEditOwnProposal(topic, proposer.getEmail())) {
      return "redirect:/dot/" + periodId + "/de-tai";
    }
    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, topic, form, proposer);
    return "page/topic/create";
  }

  public String update(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) {
    if (principal == null) return "redirect:/dot/" + periodId + "/de-tai";
    User proposer = currentUser(principal);
    Topic existing = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!topicService.canEditOwnProposal(existing, proposer.getEmail())) {
      return "redirect:/dot/" + periodId + "/de-tai";
    }
    if (errors.hasErrors()) {
      prepare(model, existing, form, proposer);
      return "page/topic/create";
    }
    try {
      topicService.updateProposal(topicId, periodId, proposer, form.toEntity(), form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepare(model, existing, form, proposer);
      return "page/topic/create";
    }
    return "redirect:/dot/" + periodId + "/de-tai/" + topicId;
  }

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
    return "redirect:/dot/" + periodId + "/de-tai";
  }

  private void prepare(Model model, Topic topic, TopicForm form, User proposer) {
    Long periodId = topic.getRegistrationPeriod().getId();
    Long topicId = topic.getId();

    if (form.getAdviserIds() == null) form.setAdviserIds(new LinkedHashSet<>());
    form.getAdviserIds().add(proposer.getId());
    crudView.form(
        model,
        form,
        "/dot/" + periodId + "/de-tai/" + topicId,
        "post",
        "/dot/" + periodId + "/de-tai",
        "w-full",
        false,
        "fragment/feature/topic/fields",
        validation.render(TopicForm.class));
    model.addAttribute("periodId", periodId);
    model.addAttribute("topicId", topicId);
    model.addAttribute("departments", departmentService.findAll());
    model.addAttribute("selectedLecturers", userService.findAllLecturersByIds(form.getAdviserIds()));
    model.addAttribute("requiredAdviserId", proposer.getId());
    model.addAttribute("lecturerSearchUrl", "/dot/" + periodId + "/de-tai/giang-vien");
    model.addAttribute("pageTitle", "topics.page.edit.title");
    model.addAttribute("pageIcon", "bi:journal-text");
    model.addAttribute("showProposalRules", true);
    model.addAttribute("breadcrumbParentUrl", "/dot/" + periodId + "/de-tai");
    model.addAttribute("breadcrumbParentLabel", topic.getRegistrationPeriod().getName());
  }
  private User currentUser(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
}
