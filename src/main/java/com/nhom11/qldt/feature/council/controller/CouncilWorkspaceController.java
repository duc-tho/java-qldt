package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.dto.FinalResultForm;
import com.nhom11.qldt.feature.council.model.entity.Evaluation;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.report.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/dot/{periodId}/danh-gia")
public class CouncilWorkspaceController {
  private final CouncilService councilService;
  private final ReportSubmissionService reportSubmissionService;
  private final UserService userService;
  private final RegistrationPeriodService periodService;

  public CouncilWorkspaceController(
      CouncilService councilService,
      ReportSubmissionService reportSubmissionService,
      UserService userService,
      RegistrationPeriodService periodService) {
    this.councilService = councilService;
    this.reportSubmissionService = reportSubmissionService;
    this.userService = userService;
    this.periodService = periodService;
  }

  @GetMapping
  public String list(@PathVariable Long periodId, @AuthenticationPrincipal UserDetails principal, Model model) {
    if (periodService.findById(periodId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    User member = user(principal);
    model.addAttribute("registrations", councilService.workspaceRegistrations(member).stream()
        .filter(registration -> registration.getTopic().getRegistrationPeriod().getId().equals(periodId))
        .toList());
    model.addAttribute("periodId", periodId);
    model.addAttribute("pageTitle", "councils.workspace.list.title");
    model.addAttribute("pageIcon", "bi:clipboard2-check");
    return "page/evaluation/list";
  }

  @GetMapping("/{registrationId}")
  public String detail(
      @PathVariable Long periodId, @PathVariable Long registrationId,
      @AuthenticationPrincipal UserDetails principal, Model model) {
    try {
      User member = user(principal);
      TopicRegistration registration = councilService.workspaceRegistration(registrationId, member);
      if (!registration.getTopic().getRegistrationPeriod().getId().equals(periodId)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      Evaluation evaluation = councilService.evaluation(registrationId, member.getId());
      EvaluationForm evaluationForm = new EvaluationForm();
      if (evaluation != null) {
        evaluationForm.setScore(evaluation.getScore());
        evaluationForm.setComment(evaluation.getComment());
      }
      model.addAttribute("registration", registration);
      ReportSubmission currentReport = reportSubmissionService.current(registrationId);
      model.addAttribute("currentReport", currentReport);
      model.addAttribute(
          "reportFiles",
          currentReport == null ? List.of() : reportSubmissionService.files(currentReport.getId()));
      model.addAttribute("evaluations", councilService.evaluations(registrationId));
      model.addAttribute("finalResult", councilService.finalResult(registrationId));
      model.addAttribute("evaluation", evaluation);
      model.addAttribute("evaluationForm", evaluationForm);
      model.addAttribute("finalResultForm", new FinalResultForm());
      model.addAttribute("canEvaluate", councilService.canEvaluate(registrationId, member));
      model.addAttribute("canFinalize", councilService.canFinalizeResult(registrationId, member));
      model.addAttribute(
          "councilEvaluation",
          registration.getTopic().getRegistrationPeriod().getPeriodType() == PeriodType.KLTN);
      model.addAttribute("pageTitle", "councils.workspace.title");
      model.addAttribute("pageIcon", "bi:clipboard2-check");
      return "page/evaluation/detail";
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  private User user(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }
}
