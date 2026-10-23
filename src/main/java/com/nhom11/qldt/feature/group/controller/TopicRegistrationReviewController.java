package com.nhom11.qldt.feature.group.controller;

import com.nhom11.qldt.core.view.WorkPageViewSupport;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
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
/** Điều phối các yêu cầu HTTP của chức năng nhóm sinh viên và đăng ký đề tài. */
public class TopicRegistrationReviewController {
  private final StudentGroupService groupService;
  private final UserService userService;
  private final WorkPageViewSupport workPageView;

  public TopicRegistrationReviewController(
      StudentGroupService groupService,
      UserService userService,
      WorkPageViewSupport workPageView) {
    this.groupService = groupService;
    this.userService = userService;
    this.workPageView = workPageView;
  }

  @GetMapping("/phe-duyet-dang-ky")
  /** Hiển thị danh sách nhóm sinh viên và đăng ký đề tài mà người dùng được phép truy cập. */
  public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
    User lecturer = userService.findByEmail(principal.getUsername()).orElseThrow();
    Map<RegistrationPeriod, List<TopicRegistration>> registrationsByPeriod = new LinkedHashMap<>();
    groupService.adviserRegistrations(lecturer).forEach(registration ->
        registrationsByPeriod.computeIfAbsent(
            registration.getStudentGroup().getRegistrationPeriod(), ignored -> new ArrayList<>())
            .add(registration));

    List<PeriodWorkCard> pending = new ArrayList<>();
    List<PeriodWorkCard> history = new ArrayList<>();
    registrationsByPeriod.forEach((period, registrations) -> {
      long pendingCount = registrations.stream()
          .filter(registration -> registration.getStatus() == TopicRegistrationStatus.PENDING)
          .count();
      PeriodWorkCard card = new PeriodWorkCard(
          period,
          "/dot/" + period.getId() + "/nhom",
          workPageView.message(pendingCount > 0
              ? "work.registration.pending" : "work.registration.history"),
          pendingCount > 0 ? "badge-warning" : "badge-success",
          workPageView.message("work.metric.total", registrations.size()),
          workPageView.message("work.metric.pending", pendingCount),
          workPageView.message("work.action.open"));
      (pendingCount > 0 ? pending : history).add(card);
    });

    model.addAttribute("workSections", List.of(
        workPageView.section("registration-pending", "work.registration.pending",
            "work.registration.pending.empty", pending),
        workPageView.section("registration-history", "work.registration.history",
            "work.registration.history.empty", history)));
    workPageView.page(
        model, "work.registration.title", "work.registration.description", "bi:clipboard-check");
    return "page/work/periods";
  }
}
