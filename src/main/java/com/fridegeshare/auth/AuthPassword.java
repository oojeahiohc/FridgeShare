package com.fridegeshare.auth;

import java.nio.charset.StandardCharsets;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.Inputs;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class AuthPassword implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence raw) {
        return bcrypt.encode(normalize(raw));
    }

    @Override
    public boolean matches(CharSequence raw, String encoded) {
        try {
            return bcrypt.matches(normalize(raw), encoded);
        } catch (ApiException invalidPassword) {
            // 로그인에서는 입력 형식 오류도 일반 인증 실패로 처리한다.
            return false;
        }
    }

    // 가입, 로그인의 공통 공백 처리 및 BCrypt 바이트 상한 검증
    private String normalize(CharSequence raw) {
        String value = Inputs.text(raw == null ? null : raw.toString(), "비밀번호", 8, 64);
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.invalid("비밀번호는 UTF-8 기준 최대 72바이트입니다.");
        }
        return value;
    }
}