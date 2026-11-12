package com.jiwoong.fashionfit.bodyprofile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jiwoong.fashionfit.auth.LoginUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// "내" 신체 정보라서 주소에 userId 를 받지 않는다. 누구 것인지는 로그인 세션(LoginUser)이 정한다.
// (/api/users/{id}/body-profile 로 만들면 남의 id 를 넣어 보는 공격을 따로 막아야 한다)
@RestController
@RequestMapping("/api/me/body-profile")
@RequiredArgsConstructor
public class BodyProfileController {

    private final BodyProfileService bodyProfileService;

    @GetMapping
    public ResponseEntity<BodyProfileResponse> get(@AuthenticationPrincipal LoginUser loginUser) {
        return bodyProfileService.find(loginUser.userId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public BodyProfileResponse save(@AuthenticationPrincipal LoginUser loginUser,
                                    @Valid @RequestBody BodyProfileRequest request) {
        return bodyProfileService.save(loginUser.userId(), request);
    }
}
