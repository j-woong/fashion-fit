package com.jiwoong.fashionfit.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

// 로그인에 성공하면 세션에 저장되는 "현재 사용자".
// 컨트롤러에서 @AuthenticationPrincipal LoginUser 로 꺼내 쓴다.
public record LoginUser(Long userId, String email, String nickname, Map<String, Object> attributes)
        implements OAuth2User, Serializable {

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
