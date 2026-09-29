package com.nhom11.qldt.feature.studentgroup.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.studentgroup.model.dto.RejectTopicRegistrationForm;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.studentgroup.service.StudentGroupService;
import jakarta.validation.Valid;
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
@RequestMapping("/topic-registrations")
public class TopicRegistrationReviewController {
  private final StudentGroupService studentGroupService;
  private final UserService userService;

  public TopicRegistrationReviewController(StudentGroupService studentGroupService, UserService userService) {
    this.studentGroupService = studentGroupService;
    this.userService = userService;
  }

  @GetMapping
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) return "redirect:/login";
    model.addAttribute("registrations", studentGroupService.adviserRegistrations(currentUser(principal)));
    model.addAttribute("pageTitle", "topic-registrations.page.list.title");
    model.addAttribute("pageIcon", "bi:journal-check");
    return "fragment/page/topic-registration/list";
  }

  @GetMapping("/{registrationId}")
  public String detail(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) return "redirect:/login";
    TopicRegistration registration = studentGroupService.adviserRegistration(registrationId, currentUser(principal));
    if (registration == null) return "redirect:/topic-registrations";
    populate(model, registration);
    return "fragment/page/topic-registration/detail";
  }

  @PostMapping("/{registrationId}/approve")
  public String approve(
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes redirectAttributes) {
    try { studentGroupService.approveRegistration(registrationId, currentUser(principal)); }
    catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/topic-registrations/" + registrationId;
  }

  @PostMapping("/{registrationId}/reject")
  public String reject(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute("rejectForm") RejectTopicRegistrationForm form, BindingResult errors, Model model) {
    if (principal == null) return "redirect:/login";
    try {
      if (!errors.hasErrors()) studentGroupService.rejectRegistration(registrationId, currentUser(principal), form.getReason());
    } catch (IllegalArgumentException exception) {
      errors.reject(exception.getMessage());
    }
    if (!errors.hasErrors()) return "redirect:/topic-registrations/" + registrationId;
    TopicRegistration registration = studentGroupService.adviserRegistration(registrationId, currentUser(principal));
    if (registration == null) return "redirect:/topic-registrations";
    populate(model, registration);
    return "fragment/page/topic-registration/detail";
  }

  private void populate(Model model, TopicRegistration registration) {
    model.addAttribute("registration", registration);
    model.addAttribute("members", studentGroupService.members(registration.getStudentGroup().getId()));
    model.addAttribute("isOpen", studentGroupService.isOpen(registration.getStudentGroup().getRegistrationPeriod()));
    model.addAttribute("isPending", registration.getStatus() == TopicRegistrationStatus.PENDING);
    if (!model.containsAttribute("rejectForm")) {
      model.addAttribute("rejectForm", new RejectTopicRegistrationForm());
    }
    model.addAttribute("pageTitle", "topic-registrations.page.detail.title");
    model.addAttribute("pageIcon", "bi:journal-check");
  }

  private User currentUser(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
}
