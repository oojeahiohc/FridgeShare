package com.fridegeshare.auth;

import java.util.Map;

import com.fridegeshare.common.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuthApiController {
    private final AuthService auth;

    AuthApiController(AuthService auth) {
        this.auth = auth;
    }

    // 가입 요청은 계정을 생성하고, 세션 인증은 로그인 폼 제출에서 이루어진다.
    @PostMapping("/api/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, AuthService.Me> signup(@RequestBody AuthService.Signup form) {
        return Map.of("data", auth.signup(form));
    }

    @GetMapping("/api/me")
    Map<String, AuthService.Me> me(@AuthenticationPrincipal SessionUser user) {
        return Map.of("data", auth.me(user));
    }
}