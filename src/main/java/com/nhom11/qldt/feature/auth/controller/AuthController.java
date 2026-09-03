package com.nhom11.qldt.feature.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    @GetMapping("/auth/login")
    public String loginView() {
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String login(Model model) {

        return "auth/login";
    }
}
