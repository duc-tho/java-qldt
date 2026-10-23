package com.nhom11.qldt.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/** Điều phối trang cài đặt chung của ứng dụng. */
public class SettingsController {

  @GetMapping("/cai-dat")
  /** Hiển thị trang cài đặt với tiêu đề và biểu tượng tương ứng. */
  public String settings(Model model) {
    model.addAttribute("pageTitle", "settings.page.title");
    model.addAttribute("pageIcon", "bi:gear");
    return "page/settings/index";
  }
}
