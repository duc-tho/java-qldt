package com.nhom11.qldt.feature.council.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LegacyCouncilController {
  @GetMapping("/registration-periods/{periodId}/councils")
  public String councils(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/hoi-dong";
  }

  @GetMapping("/registration-periods/{periodId}/councils/new")
  public String create(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/hoi-dong/tao";
  }

  @GetMapping("/registration-periods/{periodId}/councils/{councilId}")
  public String detail(@PathVariable Long periodId, @PathVariable Long councilId) {
    return "redirect:/dot/" + periodId + "/hoi-dong/" + councilId;
  }

  @GetMapping("/registration-periods/{periodId}/councils/{councilId}/edit")
  public String edit(@PathVariable Long periodId, @PathVariable Long councilId) {
    return detail(periodId, councilId) + "/sua";
  }

  @GetMapping("/registration-periods/{periodId}/reviewer-assignments")
  public String assignments(@PathVariable Long periodId) {
    return "redirect:/dot/" + periodId + "/phan-cong";
  }
}
