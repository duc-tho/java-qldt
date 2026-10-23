package com.nhom11.qldt.feature.council.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.council.model.dto.PublicationRevocationForm;
import com.nhom11.qldt.feature.council.service.CouncilService;
import com.nhom11.qldt.feature.group.model.dto.GroupSummary;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
/** Điều phối các yêu cầu HTTP của chức năng hội đồng, đánh giá và công bố kết quả. */
public class ResultPageController {
  private final CouncilService councilService;
  private final RegistrationPeriodService periodService;
  private final StudentGroupService groupService;
  private final UserService userService;

  public ResultPageController(CouncilService councilService, RegistrationPeriodService periodService,
      StudentGroupService groupService, UserService userService) {
    this.councilService = councilService;
    this.periodService = periodService;
    this.groupService = groupService;
    this.userService = userService;
  }

  @GetMapping("/dot/{periodId}/ket-qua")
  /** Hiển thị thông tin chi tiết của hội đồng và kết quả đánh giá được yêu cầu. */
  public String detail(@PathVariable Long periodId, Authentication authentication, Model model) {
    RegistrationPeriod period = periodService.findById(periodId);
    if (period == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    boolean isStaff = hasRole(authentication, "ROLE_ADMIN")
        || hasRole(authentication, "ROLE_HEAD_OF_FACULTY");
    model.addAttribute("period", period);
    model.addAttribute("isStaff", isStaff);
    model.addAttribute("pageTitle", "result-publications.title");
    model.addAttribute("pageIcon", "bi:award");
    model.addAttribute("breadcrumbParentUrl", "/dot/" + periodId);
    model.addAttribute("breadcrumbParentLabel", period.getName());

    if (isStaff) {
      model.addAttribute("latestPublication", councilService.latestPublication(periodId));
      model.addAttribute("publicationHistory", councilService.publicationHistory(periodId));
      model.addAttribute("resultSummaries", councilService.resultSummaries(periodId));
      model.addAttribute("canPublishResults", councilService.canPublishResults(periodId));
      model.addAttribute("revocationForm", new PublicationRevocationForm());
      return "page/result/detail";
    }

    if (!councilService.resultsPublished(periodId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    User viewer = userService.findByEmail(authentication.getName()).orElseThrow();
    boolean isLecturer = hasRole(authentication, "ROLE_LECTURER");
    boolean isStudent = hasRole(authentication, "ROLE_STUDENT");
    boolean visibleAsLecturer = isLecturer && periodService.findVisibleForLecturer(viewer.getId())
        .stream().anyMatch(item -> item.getId().equals(periodId));
    boolean visibleAsStudent = isStudent && periodService.findVisibleForStudent(viewer.getId())
        .stream().anyMatch(item -> item.getId().equals(periodId));
    if (!visibleAsLecturer && !visibleAsStudent) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    List<GroupSummary> lecturerGroups = visibleAsLecturer
        ? groupService.groupSummariesForLecturer(periodId, viewer, false) : List.of();
    List<GroupSummary> studentGroups = visibleAsStudent
        ? groupService.groupSummariesForStudent(viewer).stream()
            .filter(summary -> summary.group().getRegistrationPeriod().getId().equals(periodId))
            .toList() : List.of();
    model.addAttribute("results", councilService.publishedResults(periodId,
        Stream.concat(lecturerGroups.stream(), studentGroups.stream())
            .map(summary -> summary.group().getId()).distinct().toList()));
    return "page/result/detail";
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(role));
  }
}
