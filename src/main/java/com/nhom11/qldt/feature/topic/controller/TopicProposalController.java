package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.validation.ClientValidationRuleRenderer;
import com.nhom11.qldt.core.view.CrudViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.department.service.DepartmentService;
import com.nhom11.qldt.feature.topic.model.dto.TopicForm;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topic-proposals")
public class TopicProposalController {
  private final TopicService topicService;
  private final UserService userService;
  private final DepartmentService departmentService;
  private final CrudViewSupport crudView;
  private final ClientValidationRuleRenderer validation;

  public TopicProposalController(
      TopicService topicService,
      UserService userService,
      DepartmentService departmentService,
      CrudViewSupport crudView,
      ClientValidationRuleRenderer validation) {
    this.topicService = topicService;
    this.userService = userService;
    this.departmentService = departmentService;
    this.crudView = crudView;
    this.validation = validation;
  }

  @GetMapping
  public String list() {
    return "redirect:/dot";
  }

  @GetMapping("/{periodId}/topics")
  public String topics(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/de-tai";
  }

  public String editForm(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) return "redirect:/dot/" + periodId + "/de-tai";
    User proposer = currentUser(principal);
    Topic topic = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!isProposalOwner(topic, proposer)) return "redirect:/dot/" + periodId + "/de-tai";
    if (!topicService.isProposalWindowOpen(topic.getRegistrationPeriod())) return "redirect:/dot/" + periodId + "/de-tai";
    TopicForm form = new TopicForm();
    form.fromEntity(topic);
    prepare(model, periodId, topicId, form, proposer);
    return "page/topic/create";
  }

  @GetMapping("/{periodId}/topics/{topicId}/edit")
  public String legacyEdit(@PathVariable Long periodId, @PathVariable Long topicId) {
    return "redirect:/dot/" + periodId + "/de-tai/" + topicId + "/sua";
  }

  public String update(@PathVariable Long periodId, @PathVariable Long topicId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute("data") TopicForm form, BindingResult errors, Model model) {
    if (principal == null) return "redirect:/dot/" + periodId + "/de-tai";
    User proposer = currentUser(principal);
    Topic existing = topicService.findByIdAndRegistrationPeriodId(topicId, periodId);
    if (!isProposalOwner(existing, proposer)
        || !topicService.isProposalWindowOpen(existing.getRegistrationPeriod())) {
      return "redirect:/dot/" + periodId + "/de-tai";
    }
    if (errors.hasErrors()) {
      prepare(model, periodId, topicId, form, proposer);
      return "page/topic/create";
    }
    try {
      topicService.updateProposal(topicId, periodId, proposer, form.toEntity(), form.getDepartmentId(), form.getAdviserIds());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
      prepare(model, periodId, topicId, form, proposer);
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

  private void prepare(Model model, Long periodId, Long topicId, TopicForm form, User proposer) {
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
  }
  private User currentUser(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
  private boolean isProposalOwner(Topic topic, User user) { return topic != null && (topic.getStatus() == TopicStatus.PROPOSED || topic.getStatus() == TopicStatus.REJECTED) && topic.getProposedBy() != null && topic.getProposedBy().getId().equals(user.getId()); }
}
