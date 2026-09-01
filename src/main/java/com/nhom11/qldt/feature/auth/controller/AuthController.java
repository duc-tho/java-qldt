package com.nhom11.qldt.feature.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/auth/login")
    public String loginView() {
        return "auth/login";
    }
}
