package com.fridegeshare.common;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// 로그인, 로그아웃, 세션 접근 제어
@Configuration
class SecurityConfig {
    // 화면 리다이렉트와 API 오류 응답의 분리
    @Bean
    SecurityFilterChain security(HttpSecurity http, ObjectMapper json) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // JSP 내부 포워드와 오류 화면의 재인증 방지
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD,
                                jakarta.servlet.DispatcherType.ERROR)
                        .permitAll()
                        .requestMatchers("/login", "/signup", "/api/auth/signup", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").usernameParameter("loginId")
                        .defaultSuccessUrl("/posts", true).failureUrl("/login?error"))
                .logout(logout -> logout.logoutSuccessUrl("/login"))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, error) -> {
                            if (request.getServletPath().startsWith("/api/")) {
                                write(response, json, 401, "UNAUTHORIZED", "로그인이 필요합니다.");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        })
                        .accessDeniedHandler((request, response, error) -> {
                            if (request.getServletPath().startsWith("/api/")) {
                                write(response, json, 403, "FORBIDDEN", "권한 또는 요청 토큰을 확인하세요.");
                            } else {
                                response.sendError(403);
                            }
                        }))
                .build();
    }
    // API 인증, 인가 오류의 JSON 응답 작성
    private void write(HttpServletResponse response, ObjectMapper json,
            int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getOutputStream(), Map.of("code", code, "message", message));
    }
}