package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class LegacyEvaluationController {
  private final CouncilService councilService;
  private final UserService userService;

  public LegacyEvaluationController(CouncilService councilService, UserService userService) {
    this.councilService = councilService;
    this.userService = userService;
  }

  @GetMapping("/council-workspace")
  public String list() {
    return "redirect:/dot?congViec=danh-gia";
  }

  @GetMapping("/council-workspace/{registrationId}")
  public String detail(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal) {
    if (principal == null) return "redirect:/login";
    try {
      TopicRegistration registration = councilService.workspaceRegistration(registrationId,
          userService.findByEmail(principal.getUsername()).orElseThrow());
      return "redirect:/dot/" + registration.getTopic().getRegistrationPeriod().getId()
          + "/danh-gia/" + registrationId;
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }
}
