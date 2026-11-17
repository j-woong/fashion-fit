package com.jiwoong.fashionfit.sizecheck;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jiwoong.fashionfit.auth.LoginUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 사이즈 비교
//   POST   /api/me/size-checks        실측표 입력 → 저장 + 비교 결과 (201)
//   GET    /api/me/size-checks        비교 기록 목록
//   GET    /api/me/size-checks/saved-chart?url=  같은 상품 링크로 저장해 둔 실측표 (없으면 204)
//   GET    /api/me/size-checks/{id}   비교 결과 다시 보기
//   DELETE /api/me/size-checks/{id}   기록 삭제 (204)
@RestController
@RequestMapping("/api/me/size-checks")
@RequiredArgsConstructor
public class SizeCheckController {

    private final SizeCheckService sizeCheckService;

    @PostMapping
    public ResponseEntity<SizeCheckResponse> create(@AuthenticationPrincipal LoginUser loginUser,
                                                    @Valid @RequestBody SizeCheckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sizeCheckService.create(loginUser.userId(), request));
    }

    @GetMapping
    public List<SizeCheckSummary> list(@AuthenticationPrincipal LoginUser loginUser) {
        return sizeCheckService.list(loginUser.userId());
    }

    // 없는 것이 오류가 아니라 흔한 경우라서 404 가 아니라 204 로 답한다 (프론트가 예외 처리 없이 "없음"으로 다룬다)
    @GetMapping("/saved-chart")
    public ResponseEntity<SavedSizeChartResponse> savedChart(@AuthenticationPrincipal LoginUser loginUser,
                                                             @RequestParam("url") String url) {
        return sizeCheckService.findSavedChart(loginUser.userId(), url)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{sizeCheckId}")
    public SizeCheckResponse get(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long sizeCheckId) {
        return sizeCheckService.get(loginUser.userId(), sizeCheckId);
    }

    @DeleteMapping("/{sizeCheckId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long sizeCheckId) {
        sizeCheckService.delete(loginUser.userId(), sizeCheckId);
        return ResponseEntity.noContent().build();
    }
}
