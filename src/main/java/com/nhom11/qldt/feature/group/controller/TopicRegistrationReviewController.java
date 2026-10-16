package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/topic-registrations")
public class TopicRegistrationReviewController {
  private final StudentGroupService groupService;
  private final UserService userService;

  public TopicRegistrationReviewController(StudentGroupService groupService, UserService userService) {
    this.groupService = groupService;
    this.userService = userService;
  }

  @GetMapping
  public String list() {
    return "redirect:/dot?congViec=duyet";
  }

  @GetMapping("/{registrationId}")
  public String detail(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal) {
    if (principal == null) return "redirect:/login";
    TopicRegistration registration = groupService.adviserRegistration(registrationId,
        userService.findByEmail(principal.getUsername()).orElseThrow());
    if (registration == null) return "redirect:/dot?congViec=duyet";
    return "redirect:/dot/" + registration.getStudentGroup().getRegistrationPeriod().getId()
        + "/nhom/" + registration.getStudentGroup().getId();
  }
}
