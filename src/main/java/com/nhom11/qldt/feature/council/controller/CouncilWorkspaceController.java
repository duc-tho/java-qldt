package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.EvaluationForm;
import com.nhom11.qldt.feature.council.model.dto.FinalResultForm;
import com.nhom11.qldt.feature.council.model.entity.Evaluation;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import com.nhom11.qldt.feature.reportsubmission.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.reportsubmission.service.ReportSubmissionService;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/council-workspace")
public class CouncilWorkspaceController {
  private final CouncilService councilService;
  private final ReportSubmissionService reportSubmissionService;
  private final UserService userService;

  public CouncilWorkspaceController(
      CouncilService councilService,
      ReportSubmissionService reportSubmissionService,
      UserService userService) {
    this.councilService = councilService;
    this.reportSubmissionService = reportSubmissionService;
    this.userService = userService;
  }

  @GetMapping
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User member = user(principal);
    model.addAttribute("registrations", councilService.workspaceRegistrations(member));
    model.addAttribute("pageTitle", "councils.workspace.list.title");
    model.addAttribute("pageIcon", "bi:clipboard2-check");
    return "fragment/page/council/workspace-list";
  }

  @GetMapping("/{registrationId}")
  public String detail(
      @PathVariable Long registrationId, @AuthenticationPrincipal UserDetails principal, Model model) {
    try {
      User member = user(principal);
      TopicRegistration registration = councilService.workspaceRegistration(registrationId, member);
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
      return "fragment/page/council/workspace";
    } catch (IllegalArgumentException exception) {
      return "redirect:/registration-periods";
    }
  }

  private User user(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }
}
