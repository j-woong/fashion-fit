package com.jiwoong.fashionfit.garment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jiwoong.fashionfit.auth.LoginUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 내 옷장(기준 옷) CRUD
//   GET    /api/me/garments        목록
//   GET    /api/me/garments/{id}   하나
//   POST   /api/me/garments        등록 → 201 Created
//   PUT    /api/me/garments/{id}   수정
//   DELETE /api/me/garments/{id}   삭제 → 204 No Content
@RestController
@RequestMapping("/api/me/garments")
@RequiredArgsConstructor
public class GarmentController {

    private final GarmentService garmentService;

    @GetMapping
    public List<GarmentResponse> list(@AuthenticationPrincipal LoginUser loginUser) {
        return garmentService.list(loginUser.userId());
    }

    @GetMapping("/{garmentId}")
    public GarmentResponse get(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long garmentId) {
        return garmentService.get(loginUser.userId(), garmentId);
    }

    @PostMapping
    public ResponseEntity<GarmentResponse> create(@AuthenticationPrincipal LoginUser loginUser,
                                                  @Valid @RequestBody GarmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garmentService.create(loginUser.userId(), request));
    }

    @PutMapping("/{garmentId}")
    public GarmentResponse update(@AuthenticationPrincipal LoginUser loginUser,
                                  @PathVariable Long garmentId,
                                  @Valid @RequestBody GarmentRequest request) {
        return garmentService.update(loginUser.userId(), garmentId, request);
    }

    @DeleteMapping("/{garmentId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long garmentId) {
        garmentService.delete(loginUser.userId(), garmentId);
        return ResponseEntity.noContent().build();
    }
}
