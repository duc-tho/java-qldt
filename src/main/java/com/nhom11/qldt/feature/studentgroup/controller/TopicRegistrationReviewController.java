package com.nhom11.qldt.feature.studentgroup.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.studentgroup.model.dto.RejectTopicRegistrationForm;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import com.nhom11.qldt.feature.studentgroup.service.GroupActivityService;
import com.nhom11.qldt.feature.reportsubmission.service.ReportSubmissionService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topic-registrations")
public class TopicRegistrationReviewController {
  private final StudentGroupService studentGroupService;
  private final UserService userService;
  private final GroupActivityService groupActivityService;
  private final ReportSubmissionService reportSubmissionService;

  public TopicRegistrationReviewController(
      StudentGroupService studentGroupService, UserService userService,
      GroupActivityService groupActivityService, ReportSubmissionService reportSubmissionService) {
    this.studentGroupService = studentGroupService;
    this.userService = userService;
    this.groupActivityService = groupActivityService;
    this.reportSubmissionService = reportSubmissionService;
  }

  @GetMapping
  public String list(
      @AuthenticationPrincipal UserDetails principal,
      @RequestParam(required = false) TopicRegistrationStatus status,
      Model model) {
    if (principal == null) return "redirect:/login";
    List<TopicRegistration> registrations =
        studentGroupService.adviserRegistrations(currentUser(principal));
    if (status != null) {
      registrations = registrations.stream().filter(item -> item.getStatus() == status).toList();
    }
    model.addAttribute("registrations", registrations);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("reviewStatuses", List.of(
        TopicRegistrationStatus.DRAFT,
        TopicRegistrationStatus.PENDING,
        TopicRegistrationStatus.APPROVED,
        TopicRegistrationStatus.REJECTED));
    model.addAttribute("pageTitle", "topic-registrations.page.list.title");
    model.addAttribute("pageIcon", "bi:journal-check");
    return "fragment/page/topic-registration/list";
  }

  @GetMapping("/{registrationId}")
  public String detail(
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      Model model) {
    if (principal == null) return "redirect:/login";
    TopicRegistration registration =
        studentGroupService.adviserRegistration(registrationId, currentUser(principal));
    if (registration == null) return "redirect:/topic-registrations";
    populate(model, registration);
    return "fragment/page/topic-registration/detail";
  }

  @PostMapping("/{registrationId}/approve")
  public String approve(
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    try {
      studentGroupService.approveRegistration(registrationId, currentUser(principal));
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/topic-registrations";
  }

  @PostMapping("/{registrationId}/reject")
  public String reject(
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("rejectForm") RejectTopicRegistrationForm form,
      BindingResult errors,
      Model model) {
    if (principal == null) return "redirect:/login";
    try {
      if (!errors.hasErrors())
        studentGroupService.rejectRegistration(
            registrationId, currentUser(principal), form.getReason());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
    }
    if (!errors.hasErrors()) return "redirect:/topic-registrations";
    TopicRegistration registration =
        studentGroupService.adviserRegistration(registrationId, currentUser(principal));
    if (registration == null) return "redirect:/topic-registrations";
    populate(model, registration);
    return "fragment/page/topic-registration/detail";
  }

  private void populate(Model model, TopicRegistration registration) {
    model.addAttribute("registration", registration);
    model.addAttribute(
        "members", studentGroupService.members(registration.getStudentGroup().getId()));
    model.addAttribute("activities", groupActivityService.history(registration.getStudentGroup().getId()));
    var report = registration.getStatus() == TopicRegistrationStatus.APPROVED
        ? reportSubmissionService.current(registration.getId()) : null;
    model.addAttribute("currentReport", report);
    model.addAttribute("reportFiles", report == null ? List.of() : reportSubmissionService.files(report.getId()));
    model.addAttribute(
        "isOpen",
        studentGroupService.isOpen(registration.getStudentGroup().getRegistrationPeriod()));
    model.addAttribute("isPending", registration.getStatus() == TopicRegistrationStatus.PENDING);
    if (!model.containsAttribute("rejectForm")) {
      model.addAttribute("rejectForm", new RejectTopicRegistrationForm());
    }
    model.addAttribute("pageTitle", "topic-registrations.page.detail.title");
    model.addAttribute("pageIcon", "bi:journal-check");
  }

  private User currentUser(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }
}
