package com.nhom11.qldt.feature.report.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LegacyReportController {
  @GetMapping("/registration-periods/{periodId}/student-group/{groupId}/reports/files/{fileId}")
  public String download(@PathVariable Long periodId, @PathVariable Long groupId,
      @PathVariable Long fileId) {
    return "redirect:/dot/" + periodId + "/nhom/" + groupId + "/bao-cao/tep/" + fileId;
  }
}
