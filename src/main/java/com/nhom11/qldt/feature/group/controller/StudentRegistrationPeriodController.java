package com.nhom11.qldt.feature.group.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student-groups")
public class StudentRegistrationPeriodController {
  @GetMapping
  public String list() {
    return "redirect:/dot?coNhom=true";
  }

  @GetMapping("/periods")
  public String periods() {
    return "redirect:/dot";
  }
}
