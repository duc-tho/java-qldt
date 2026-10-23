package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dot/{periodId}/phan-cong")
/** Điều phối các yêu cầu HTTP của chức năng hội đồng, đánh giá và công bố kết quả. */
public class ReviewerAssignmentController {
  private final CouncilService councilService;
  private final RegistrationPeriodService periodService;
  private final UserService userService;

  public ReviewerAssignmentController(
      CouncilService councilService,
      RegistrationPeriodService periodService,
      UserService userService) {
    this.councilService = councilService;
    this.periodService = periodService;
    this.userService = userService;
  }

  @GetMapping
  /** Hiển thị danh sách hội đồng và kết quả đánh giá mà người dùng được phép truy cập. */
  public String list(@PathVariable Long periodId, Model model) {
    RegistrationPeriod period = periodService.findById(periodId);
    if (period == null || period.getPeriodType() != PeriodType.TLCN) {
      return "redirect:/dot";
    }

    model.addAttribute("period", period);
    model.addAttribute("topics", councilService.reviewerAssignmentTopics(periodId));
    model.addAttribute("lecturers", userService.findAllLecturers());
    model.addAttribute("managementOpen", councilService.isReviewerAssignmentOpen(periodId));
    model.addAttribute("pageTitle", "reviewer-assignments.page.title");
    model.addAttribute("pageIcon", "bi:person-check");
    return "page/assignment/list";
  }

  @PostMapping("/de-tai/{topicId}")
  /** Phân công giảng viên phản biện cho đề tài. */
  public String assign(
      @PathVariable Long periodId,
      @PathVariable Long topicId,
      @RequestParam Long reviewerId,
      RedirectAttributes attributes) {
    try {
      councilService.assignReviewer(periodId, topicId, reviewerId);
    } catch (IllegalArgumentException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirect(periodId);
  }

  private String redirect(Long periodId) {
    return "redirect:/dot/" + periodId + "/phan-cong";
  }
}
