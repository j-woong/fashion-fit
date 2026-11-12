package com.jiwoong.fashionfit.bodyprofile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 프론트가 PUT /api/me/body-profile 로 보내는 값. @Valid 로 검사하고, 틀리면 400 + 필드별 메시지.
public record BodyProfileRequest(
        @Min(value = 100, message = "키는 100~250cm 사이로 입력해 주세요")
        @Max(value = 250, message = "키는 100~250cm 사이로 입력해 주세요")
        Integer heightCm,

        @Min(value = 20, message = "몸무게는 20~300kg 사이로 입력해 주세요")
        @Max(value = 300, message = "몸무게는 20~300kg 사이로 입력해 주세요")
        Integer weightKg,

        @NotNull(message = "발 사이즈는 꼭 입력해 주세요")
        @Min(value = 200, message = "발 사이즈는 200~330mm 사이로 입력해 주세요")
        @Max(value = 330, message = "발 사이즈는 200~330mm 사이로 입력해 주세요")
        Integer footSizeMm
) {
}
