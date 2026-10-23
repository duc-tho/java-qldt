package com.nhom11.qldt.feature.report.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.service.StudentGroupService;
import com.nhom11.qldt.feature.report.model.entity.ReportFile;
import com.nhom11.qldt.feature.report.service.ReportSubmissionService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/dot/{periodId}/nhom/{groupId}/bao-cao")
/** Điều phối các yêu cầu HTTP của chức năng nộp báo cáo. */
public class ReportSubmissionController {
  private final ReportSubmissionService reportSubmissionService;
  private final UserService userService;
  private final StudentGroupService groupService;

  public ReportSubmissionController(
      ReportSubmissionService reportSubmissionService, UserService userService,
      StudentGroupService groupService) {
    this.reportSubmissionService = reportSubmissionService;
    this.userService = userService;
    this.groupService = groupService;
  }

  @PostMapping
  /** Tiếp nhận và xử lý yêu cầu nộp báo cáo. */
  public String submit(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @RequestParam("files") List<MultipartFile> files,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    if (groupService.findGroup(groupId, periodId) == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    TopicRegistration registration = groupService.registration(groupId);
    if (registration == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    try {
      reportSubmissionService.submit(periodId, groupId, registration.getId(), user(principal), files);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirect(periodId, groupId);
  }

  @GetMapping("/tep/{fileId}")
  /** Kiểm tra quyền truy cập rồi trả về tệp báo cáo để tải xuống. */
  public ResponseEntity<Resource> download(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @PathVariable Long fileId,
      @AuthenticationPrincipal UserDetails principal) {
    ReportFile reportFile = reportSubmissionService.findFile(fileId);
    Resource resource =
        reportSubmissionService.download(periodId, groupId, fileId, user(principal));
    String filename = reportFile.getOriginalName();
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
        .body(resource);
  }

  private User user(UserDetails principal) {
    return userService.findByEmail(principal.getUsername()).orElseThrow();
  }

  private String redirect(Long periodId, Long groupId) {
    return "redirect:/dot/" + periodId + "/nhom/" + groupId;
  }
}
