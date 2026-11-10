package com.fridegeshare.auth;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.Inputs;
import com.fridegeshare.common.SessionUser;
import com.fridegeshare.region.RegionDirectory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {
    public record Signup(String loginId, String password, String nickname, String neighborhoodCode) {
    }

    public record Me(long id, String loginId, String nickname, String neighborhoodCode, String neighborhood) {
    }

    private final UserMapper users;
    private final AuthPassword passwords;
    private final RegionDirectory regions;

    AuthService(UserMapper users, AuthPassword passwords, RegionDirectory regions) {
        this.users = users;
        this.passwords = passwords;
        this.regions = regions;
    }

    // 등록된 행정동 코드만 저장하며, 중복 아이디는 DB 제약으로 판별한다.
    @Transactional
    public Me signup(Signup input) {
        String loginId = Inputs.text(input.loginId(), "아이디", 4, 50);
        String nickname = Inputs.text(input.nickname(), "닉네임", 1, 30);
        var region = regions.require(Inputs.text(input.neighborhoodCode(), "행정동 코드", 10, 10));
        String hash = passwords.encode(input.password());
        try {
            users.insert(loginId, hash, nickname, region.code());
        } catch (DuplicateKeyException duplicate) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.");
        }
        var saved = users.byLoginId(loginId);
        return new Me(saved.id(), saved.loginId(), saved.nickname(), region.code(), region.label());
    }

    // Spring Security가 호출한다. 조회 결과의 해시는 로그인 검증에 사용된다.
    @Override
    public SessionUser loadUserByUsername(String username) {
        String loginId = username == null ? "" : username.strip();
        int length = loginId.codePointCount(0, loginId.length());
        if (length < 4 || length > 50)
            throw new UsernameNotFoundException("로그인 정보가 올바르지 않습니다.");
        var row = users.byLoginId(loginId);
        if (row == null)
            throw new UsernameNotFoundException("로그인 정보가 올바르지 않습니다.");
        return new SessionUser(row.id(), row.loginId(), row.passwordHash(), row.nickname(), row.neighborhood());
    }

    // 회원 정보는 세션에서 가져오고, 행정동 표기명은 코드로 해석한다.
    public Me me(SessionUser user) {
        return new Me(user.id(), user.getUsername(), user.nickname(), user.neighborhoodCode(),
                regions.label(user.neighborhoodCode()));
    }
}