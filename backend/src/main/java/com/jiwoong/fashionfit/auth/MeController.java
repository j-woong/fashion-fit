package com.jiwoong.fashionfit.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jiwoong.fashionfit.bodyprofile.BodyProfileService;

import lombok.RequiredArgsConstructor;

// 프론트가 "지금 로그인한 사람 누구야?"를 물어보는 API: GET /api/me
// 로그인 안 했으면 SecurityConfig 설정 때문에 여기까지 오지 않고 401이 나간다.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final BodyProfileService bodyProfileService;

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal LoginUser loginUser) {
        return MeResponse.of(loginUser, bodyProfileService.exists(loginUser.userId()));
    }
}
