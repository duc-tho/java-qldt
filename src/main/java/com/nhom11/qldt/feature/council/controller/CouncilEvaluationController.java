package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.dto.FinalResultForm;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/dot/{periodId}/danh-gia")
/** Điều phối các yêu cầu HTTP của chức năng hội đồng, đánh giá và công bố kết quả. */
public class CouncilEvaluationController {
  private final CouncilService councilService;
  private final UserService userService;

  public CouncilEvaluationController(CouncilService councilService, UserService userService) {
    this.councilService = councilService;
    this.userService = userService;
  }

  @PostMapping("/{registrationId}/luu-nhap")
  /** Lưu phiếu đánh giá ở trạng thái bản nháp. */
  public String saveDraft(
      @PathVariable Long periodId,
      @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal,
      @Valid @ModelAttribute EvaluationForm form,
      BindingResult errors,
      org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    requirePeriod(periodId, registrationId, principal);
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", evaluationError(errors));
      return redirect(periodId, registrationId);
    }
    return save(periodId, registrationId, principal, form, false, attributes);
  }

  @PostMapping("/{registrationId}/nop")
  /** Tiếp nhận và xử lý yêu cầu nộp hội đồng và kết quả đánh giá. */
  public String submit(@PathVariable Long periodId, @PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute EvaluationForm form, BindingResult errors, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    requirePeriod(periodId, registrationId, principal);
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", evaluationError(errors));
      return redirect(periodId, registrationId);
    }
    return save(periodId, registrationId, principal, form, true, attributes);
  }

  @PostMapping("/{registrationId}/chot")
  /** Chốt kết quả cuối cùng của đăng ký sau khi kiểm tra dữ liệu. */
  public String finalizeResult(@PathVariable Long periodId, @PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, @Valid @ModelAttribute FinalResultForm form, BindingResult errors, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    requirePeriod(periodId, registrationId, principal);
    if (errors.hasErrors()) {
      attributes.addFlashAttribute("error", "final-results.validation.summary.max");
      return redirect(periodId, registrationId);
    }
    try { councilService.finalizeResult(registrationId, user(principal), form); } catch (IllegalArgumentException exception) { attributes.addFlashAttribute("error", exception.getMessage()); }
    return redirect(periodId, registrationId);
  }

  private String save(Long periodId, Long registrationId, UserDetails principal, EvaluationForm form, boolean submit, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
    try { councilService.saveEvaluation(registrationId, user(principal), form, submit); } catch (IllegalArgumentException exception) { attributes.addFlashAttribute("error", exception.getMessage()); }
    return redirect(periodId, registrationId);
  }

  private User user(UserDetails principal) { return userService.findByEmail(principal.getUsername()).orElseThrow(); }
  private String evaluationError(BindingResult errors) {
    return errors.hasFieldErrors("comment")
        ? "evaluations.validation.comment.max"
        : "evaluations.validation.score.range";
  }

  private void requirePeriod(Long periodId, Long registrationId, UserDetails principal) {
    TopicRegistration registration;
    try {
      registration = councilService.workspaceRegistration(registrationId, user(principal));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (!registration.getTopic().getRegistrationPeriod().getId().equals(periodId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  private String redirect(Long periodId, Long registrationId) {
    return "redirect:/dot/" + periodId + "/danh-gia/" + registrationId;
  }
}
