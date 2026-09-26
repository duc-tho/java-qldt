package com.nhom11.qldt.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "settings.page.title");
        model.addAttribute("pageIcon", "bi:gear");
        return "settings";
    }
}
