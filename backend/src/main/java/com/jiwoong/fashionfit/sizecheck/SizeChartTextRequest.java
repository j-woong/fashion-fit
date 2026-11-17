package com.jiwoong.fashionfit.sizecheck;

import com.jiwoong.fashionfit.garment.GarmentCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// POST /api/me/size-chart-extractions/text 본문
// 예: { "category": "OUTER", "text": "M - 어깨 52 / 가슴 58 / 소매 62 / 총장 68" }
public record SizeChartTextRequest(
        @NotNull(message = "옷 종류를 골라 주세요")
        GarmentCategory category,

        @NotBlank(message = "실측이 적힌 글자를 붙여 넣어 주세요")
        @Size(max = 5000, message = "글자가 너무 길어요. 사이즈가 적힌 부분만 붙여 넣어 주세요")
        String text
) {
}
