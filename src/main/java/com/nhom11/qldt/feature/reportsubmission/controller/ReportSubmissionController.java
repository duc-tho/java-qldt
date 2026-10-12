package com.nhom11.qldt.feature.reportsubmission.controller;

import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.service.UserService;
import com.nhom11.qldt.feature.reportsubmission.model.entity.ReportFile;
import com.nhom11.qldt.feature.reportsubmission.service.ReportSubmissionService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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

@Controller
@RequestMapping("/registration-periods/{periodId}/student-group/{groupId}/reports")
public class ReportSubmissionController {
  private final ReportSubmissionService reportSubmissionService;
  private final UserService userService;

  public ReportSubmissionController(
      ReportSubmissionService reportSubmissionService, UserService userService) {
    this.reportSubmissionService = reportSubmissionService;
    this.userService = userService;
  }

  @PostMapping("/{registrationId}")
  public String submit(
      @PathVariable Long periodId,
      @PathVariable Long groupId,
      @PathVariable Long registrationId,
      @RequestParam("files") List<MultipartFile> files,
      @AuthenticationPrincipal UserDetails principal,
      RedirectAttributes attributes) {
    try {
      reportSubmissionService.submit(periodId, groupId, registrationId, user(principal), files);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      attributes.addFlashAttribute("error", exception.getMessage());
    }
    return redirect(periodId, groupId);
  }

  @GetMapping("/files/{fileId}")
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
    return "redirect:/registration-periods/" + periodId + "/student-group/" + groupId;
  }
}
