package com.nhom11.qldt.feature.topic.controller;

import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import com.nhom11.qldt.feature.topic.service.TopicService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/topic-proposals")
public class TopicProposalController {
  private final RegistrationPeriodService registrationPeriodService;
  private final TopicService topicService;

  public TopicProposalController(
      RegistrationPeriodService registrationPeriodService, TopicService topicService) {
    this.registrationPeriodService = registrationPeriodService;
    this.topicService = topicService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("periods", registrationPeriodService.findVisibleForLecturers());
    model.addAttribute("now", LocalDateTime.now());
    model.addAttribute("pageTitle", "topics.page.propose.title");
    model.addAttribute("pageIcon", "bi:journal-plus");
    return "fragment/page/topic-proposal/periods";
  }

  @GetMapping("/{periodId}/topics")
  public String topics(
      @org.springframework.web.bind.annotation.PathVariable Long periodId, Model model) {
    var period =
        registrationPeriodService.findVisibleForLecturers().stream()
            .filter(value -> value.getId().equals(periodId))
            .findFirst()
            .orElse(null);
    if (period == null) {
      return "redirect:/topic-proposals";
    }

    LocalDateTime now = LocalDateTime.now();
    boolean isProposalOpen =
        !now.isBefore(period.getLecturerRegistrationStart())
            && !now.isAfter(period.getLecturerRegistrationEnd());
    model.addAttribute("period", period);
    model.addAttribute("topics", topicService.findApprovedByRegistrationPeriodId(periodId));
    model.addAttribute("isProposalOpen", isProposalOpen);
    model.addAttribute("pageTitle", "topics.page.lecturer-list.title");
    model.addAttribute("pageIcon", "bi:journal-text");
    return "fragment/page/topic-proposal/topics";
  }
}
