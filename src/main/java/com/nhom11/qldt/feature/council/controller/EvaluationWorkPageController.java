package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.core.view.WorkPageViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.enums.EvaluationStatus;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.period.model.dto.PeriodWorkCard;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EvaluationWorkPageController {
  private final CouncilService councilService;
  private final UserService userService;
  private final WorkPageViewSupport workPageView;

  public EvaluationWorkPageController(
      CouncilService councilService,
      UserService userService,
      WorkPageViewSupport workPageView) {
    this.councilService = councilService;
    this.userService = userService;
    this.workPageView = workPageView;
  }

  @GetMapping("/cham-diem")
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User lecturer = userService.findByEmail(principal.getUsername()).orElseThrow();
    List<TopicRegistration> registrations = councilService.workspaceRegistrations(lecturer);
    Map<Long, EvaluationStatus> statuses = councilService.evaluationStatuses(
        registrations.stream().map(TopicRegistration::getId).toList(), lecturer.getId());
    Map<RegistrationPeriod, List<TopicRegistration>> registrationsByPeriod = new LinkedHashMap<>();
    registrations.forEach(registration -> registrationsByPeriod.computeIfAbsent(
        registration.getTopic().getRegistrationPeriod(), ignored -> new ArrayList<>()).add(registration));

    List<PeriodWorkCard> pending = new ArrayList<>();
    List<PeriodWorkCard> completed = new ArrayList<>();
    registrationsByPeriod.forEach((period, assigned) -> {
      long pendingCount = assigned.stream()
          .filter(registration -> statuses.get(registration.getId()) != EvaluationStatus.SUBMITTED)
          .count();
      PeriodWorkCard card = new PeriodWorkCard(
          period,
          "/dot/" + period.getId() + "/danh-gia",
          workPageView.message(pendingCount > 0
              ? "work.evaluation.pending" : "work.evaluation.completed"),
          pendingCount > 0 ? "badge-warning" : "badge-success",
          workPageView.message("work.evaluation.metric.total", assigned.size()),
          workPageView.message("work.evaluation.metric.pending", pendingCount),
          workPageView.message("work.action.open"));
      (pendingCount > 0 ? pending : completed).add(card);
    });

    model.addAttribute("workSections", List.of(
        workPageView.section("evaluation-pending", "work.evaluation.pending",
            "work.evaluation.pending.empty", pending),
        workPageView.section("evaluation-completed", "work.evaluation.completed",
            "work.evaluation.completed.empty", completed)));
    workPageView.page(
        model, "work.evaluation.title", "work.evaluation.description", "bi:clipboard-data");
    return "page/work/periods";
  }
}
