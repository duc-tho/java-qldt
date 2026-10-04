package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.dto.FinalResultForm;
import com.nhom11.qldt.feature.council.service.CouncilService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/council-evaluations")
public class CouncilEvaluationController {
  private final CouncilService councilService;
  private final UserService userService;

  public CouncilEvaluationController(CouncilService councilService, UserService userService) {
    this.councilService = councilService;
    this.userService = userService;
  }

  @PostMapping("/{registrationId}/draft")
  public String saveDraft(
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute EvaluationForm form,
      BindingResult errors,
      org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", evaluationError(errors));
      return redirect(registrationId);
    }
    return save(registrationId, principal, form, false, attributes);
  }

  @PostMapping("/{registrationId}/submit")
  public String submit(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute EvaluationForm form, BindingResult errors, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", evaluationError(errors));
      return redirect(registrationId);
    }
    return save(registrationId, principal, form, true, attributes);
  }

  @PostMapping("/{registrationId}/finalize")
  public String finalizeResult(@PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute FinalResultForm form, BindingResult errors, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", "final-results.validation.summary.max");
      return redirect(registrationId);
    }
    try { councilService.finalizeResult(registrationId, user(principal), form); } catch (IllegalArgumentException exception) { attributes.addFlashAttribute("error", exception.getMessage()); }
    return redirect(registrationId);
  }

  private String save(Long registrationId, UserDetails principal, EvaluationForm form, boolean submit, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try { councilService.saveEvaluation(registrationId, user(principal), form, submit); } catch (IllegalArgumentException exception) { attributes.addFlashAttribute("error", exception.getMessage()); }
    return redirect(registrationId);
  }

  private User user(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
  private String evaluationError(BindingResult errors) {
    return errors.hasFieldErrors("comment")
        ? "evaluations.validation.comment.max"
        : "evaluations.validation.score.range";
  }

  private String redirect(Long registrationId) { return "redirect:/council-workspace/" + registrationId; }
}
