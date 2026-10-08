package com.nhom11.qldt.feature.studentgroup.controller;

import com.nhom11.qldt.feature.registrationperiod.service.RegistrationPeriodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student-groups")
public class StudentRegistrationPeriodController {
  private final RegistrationPeriodService registrationPeriodService;

  public StudentRegistrationPeriodController(RegistrationPeriodService registrationPeriodService) {
    this.registrationPeriodService = registrationPeriodService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("periods", registrationPeriodService.findOpenForStudents());
    model.addAttribute("pageTitle", "student-groups.page.periods.title");
    model.addAttribute("pageIcon", "bi:calendar-range");
    return "fragment/page/student-group/periods";
  }
}
