package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.core.view.WorkPageViewSupport;
import com.nhom11.qldt.feature.period.model.dto.PeriodWorkCard;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopicApprovalWorkPageController {
  private final RegistrationPeriodService periodService;
  private final TopicService topicService;
  private final WorkPageViewSupport workPageView;

  public TopicApprovalWorkPageController(
      RegistrationPeriodService periodService,
      TopicService topicService,
      WorkPageViewSupport workPageView) {
    this.periodService = periodService;
    this.topicService = topicService;
    this.workPageView = workPageView;
  }

  @GetMapping("/phe-duyet-de-tai")
  public String list(Model model) {
    LocalDateTime now = LocalDateTime.now();
    List<RegistrationPeriod> periods = periodService.findAll().stream()
        .filter(period -> period.getLecturerRegistrationStart() != null
            && period.getLecturerRegistrationEnd() != null
            && !now.isBefore(period.getLecturerRegistrationStart()))
        .sorted(Comparator.comparing(RegistrationPeriod::getLecturerRegistrationEnd).reversed())
        .toList();
    List<Long> periodIds = periods.stream().map(RegistrationPeriod::getId).toList();
    List<TopicProposalSummary> summaries = topicService.findProposalSummariesByPeriodIds(periodIds);
    Map<Long, Long> totalCounts = summaries.stream().collect(
        Collectors.groupingBy(TopicProposalSummary::periodId, Collectors.counting()));
    Map<Long, Long> pendingCounts = summaries.stream()
        .filter(summary -> summary.status() == TopicStatus.PROPOSED)
        .collect(Collectors.groupingBy(TopicProposalSummary::periodId, Collectors.counting()));
    List<PeriodWorkCard> pending = new ArrayList<>();
    List<PeriodWorkCard> processed = new ArrayList<>();

    periods.forEach(period -> {
      long pendingCount = pendingCounts.getOrDefault(period.getId(), 0L);
      boolean approvalOpen = topicService.isProposalWindowOpen(period);
      boolean requiresApproval = approvalOpen && pendingCount > 0;
      String statusKey = requiresApproval
          ? "work.topic-approval.pending"
          : approvalOpen
              ? "work.topic-approval.processed"
              : "work.topic-approval.closed";
      PeriodWorkCard card = new PeriodWorkCard(
          period,
          "/dot/" + period.getId() + "/de-tai" + (requiresApproval ? "?status=PROPOSED" : ""),
          workPageView.message(statusKey),
          requiresApproval ? "badge-warning" : approvalOpen ? "badge-success" : "badge-ghost",
          workPageView.message(
              "work.topic-approval.metric.total", totalCounts.getOrDefault(period.getId(), 0L)),
          workPageView.message("work.topic-approval.metric.pending", pendingCount),
          workPageView.message("work.action.open"));
      (requiresApproval ? pending : processed).add(card);
    });

    model.addAttribute("workSections", List.of(
        workPageView.section("topic-approval-pending", "work.topic-approval.pending",
            "work.topic-approval.pending.empty", pending),
        workPageView.section("topic-approval-processed", "work.topic-approval.processed",
            "work.topic-approval.processed.empty", processed)));
    workPageView.page(
        model,
        "work.topic-approval.title",
        "work.topic-approval.description",
        "bi:journal-check");
    return "page/work/periods";
  }
}
