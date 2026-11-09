package com.fridegeshare.common;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class SessionUser implements UserDetails, CredentialsContainer {
    private final long id;
    private final String loginId;
    private String passwordHash;
    private final String nickname;
    private final String neighborhoodCode;

    public SessionUser(long id, String loginId, String passwordHash, String nickname, String neighborhoodCode) {
        this.id = id;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.neighborhoodCode = neighborhoodCode;
    }

    public long id() { return id; }
    public String nickname() { return nickname; }
    public String neighborhoodCode() { return neighborhoodCode; }

    @Override
    public String getUsername() { return loginId; }

    @Override
    public String getPassword() { return passwordHash; }

    // 인증 완료 후 세션에 남는 비밀번호 해시 제거
    @Override
    public void eraseCredentials() { passwordHash = null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }
}