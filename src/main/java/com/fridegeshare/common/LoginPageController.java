package com.fridegeshare.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 로그인 JSP 경로 연결
@Controller
class LoginPageController {
    @GetMapping("/login")
    String login() {
        return "auth/login";
    }
}