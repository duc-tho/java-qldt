package com.nhom11.qldt.core.controller;

import com.nhom11.qldt.feature.dashboard.model.DashboardData;
import com.nhom11.qldt.feature.dashboard.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/** Điều phối trang chủ và cung cấp dữ liệu bảng điều khiển theo người dùng đang đăng nhập. */
public class HomeController {
  private final DashboardService dashboardService;

  public HomeController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/")
  /** Hiển thị trang chủ cùng các số liệu và tác vụ phù hợp với tài khoản hiện tại. */
  public String index(Authentication authentication, Model model) {
    DashboardData dashboard = dashboardService.getDashboard(authentication.getName());
    model.addAttribute("dashboard", dashboard);

    return "page/home/index";
  }
}
