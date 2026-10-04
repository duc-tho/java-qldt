package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.PublicationRevocationForm;
import com.nhom11.qldt.feature.council.service.CouncilService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registration-periods/{periodId}/result-publication")
public class ResultPublicationController {
  private final CouncilService councilService;
  private final UserService userService;

  public ResultPublicationController(CouncilService councilService, UserService userService) {
    this.councilService = councilService;
    this.userService = userService;
  }

  @PostMapping
  public String publish(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    try {
      councilService.publishResults(periodId, user(principal));
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirect(periodId);
  }

  @PostMapping("/revoke")
  public String revoke(
      @PathVariable Long periodId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid PublicationRevocationForm form,
      BindingResult errors,
      RedirectAttributes attributes) {
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", "result-publications.validation.reason.required");
      return redirect(periodId);
    }
    try {
      councilService.revokePublication(periodId, user(principal), form.getReason());
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirect(periodId);
  }

  private User user(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }

  private String redirect(Long periodId) {
    return "redirect:/registration-periods/" + periodId + "/councils";
  }
}
