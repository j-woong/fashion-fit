package com.jiwoong.fashionfit.auth;

// 프론트의 src/api/auth.ts 에 있는 MeResponse 타입과 짝
// hasBodyProfile: false 면 프론트가 온보딩(신체 정보 입력) 화면을 먼저 보여준다
public record MeResponse(Long id, String email, String nickname, boolean hasBodyProfile) {

    public static MeResponse of(LoginUser user, boolean hasBodyProfile) {
        return new MeResponse(user.userId(), user.email(), user.nickname(), hasBodyProfile);
    }
}
