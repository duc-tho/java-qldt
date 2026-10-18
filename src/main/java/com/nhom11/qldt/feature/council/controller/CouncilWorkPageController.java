package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.core.view.WorkPageViewSupport;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.period.model.dto.PeriodWorkCard;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CouncilWorkPageController {
  private final RegistrationPeriodService periodService;
  private final CouncilService councilService;
  private final WorkPageViewSupport workPageView;

  public CouncilWorkPageController(
      RegistrationPeriodService periodService,
      CouncilService councilService,
      WorkPageViewSupport workPageView) {
    this.periodService = periodService;
    this.councilService = councilService;
    this.workPageView = workPageView;
  }

  @GetMapping("/hoi-dong")
  public String councils(Model model) {
    LocalDateTime now = LocalDateTime.now();
    List<RegistrationPeriod> periods = periodService.findAll().stream()
        .filter(period -> period.getPeriodType() == PeriodType.KLTN)
        .filter(period -> period.getStudentRegistrationEnd() != null
            && period.getCouncilDate() != null
            && now.isAfter(period.getStudentRegistrationEnd()))
        .sorted(periodOrder())
        .toList();
    Map<Long, Long> counts = councilService.councilCounts(periodIds(periods));
    List<PeriodWorkCard> assignable = new ArrayList<>();
    List<PeriodWorkCard> completed = new ArrayList<>();

    periods.forEach(period -> {
      boolean managementOpen = councilService.isCouncilManagementOpen(period);
      PeriodWorkCard card = new PeriodWorkCard(
          period,
          "/dot/" + period.getId() + "/hoi-dong",
          workPageView.message(managementOpen ? "work.council.status.open" : "work.council.status.closed"),
          managementOpen ? "badge-warning" : "badge-ghost",
          workPageView.message("work.council.metric", counts.getOrDefault(period.getId(), 0L)),
          null,
          workPageView.message("work.action.open"));
      (managementOpen ? assignable : completed).add(card);
    });

    model.addAttribute("workSections", List.of(
        workPageView.section("council-assignable", "work.council.assignable",
            "work.council.assignable.empty", assignable),
        workPageView.section("council-completed", "work.council.completed",
            "work.council.completed.empty", completed)));
    workPageView.page(model, "work.council.title", "work.council.description", "bi:people-fill");
    return "page/work/periods";
  }

  @GetMapping("/cong-bo-diem")
  public String publications(Model model) {
    LocalDateTime now = LocalDateTime.now();
    List<RegistrationPeriod> periods = periodService.findAll().stream()
        .filter(period -> period.getReportDeadline() != null
            && !now.isBefore(period.getReportDeadline()))
        .sorted(periodOrder())
        .toList();
    List<Long> periodIds = periodIds(periods);
    Set<Long> publishedIds = councilService.publishedPeriodIds(periodIds);
    Set<Long> publishableIds = councilService.publishablePeriodIds(periodIds);
    List<PeriodWorkCard> ready = new ArrayList<>();
    List<PeriodWorkCard> waiting = new ArrayList<>();
    List<PeriodWorkCard> published = new ArrayList<>();

    periods.forEach(period -> {
      boolean isPublished = publishedIds.contains(period.getId());
      boolean isReady = !isPublished && publishableIds.contains(period.getId());
      String statusKey = isPublished
          ? "work.publication.published"
          : isReady ? "work.publication.ready" : "work.publication.waiting";
      PeriodWorkCard card = new PeriodWorkCard(
          period,
          "/dot/" + period.getId() + "/ket-qua",
          workPageView.message(statusKey),
          isPublished ? "badge-success" : isReady ? "badge-info" : "badge-warning",
          null,
          null,
          workPageView.message("work.action.open"));
      if (isPublished) {
        published.add(card);
      } else if (isReady) {
        ready.add(card);
      } else {
        waiting.add(card);
      }
    });

    model.addAttribute("workSections", List.of(
        workPageView.section("publication-ready", "work.publication.ready",
            "work.publication.ready.empty", ready),
        workPageView.section("publication-waiting", "work.publication.waiting",
            "work.publication.waiting.empty", waiting),
        workPageView.section("publication-published", "work.publication.published",
            "work.publication.published.empty", published)));
    workPageView.page(model, "work.publication.title", "work.publication.description", "bi:award");
    return "page/work/periods";
  }

  private List<Long> periodIds(List<RegistrationPeriod> periods) {
    return periods.stream().map(RegistrationPeriod::getId).toList();
  }

  private Comparator<RegistrationPeriod> periodOrder() {
    return Comparator.comparing(
        RegistrationPeriod::getReportDeadline,
        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
  }

}
