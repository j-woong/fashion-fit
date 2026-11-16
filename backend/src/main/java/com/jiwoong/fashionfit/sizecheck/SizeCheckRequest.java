package com.jiwoong.fashionfit.sizecheck;

import java.util.List;
import java.util.Map;

import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.MeasurementPart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// POST /api/me/size-checks 본문
// 예: { "category": "LONG_SLEEVE", "productName": "오버핏 맨투맨", "referenceGarmentId": null,
//       "sizes": [ { "label": "M", "measurements": { "CHEST_WIDTH": 58, "TOTAL_LENGTH": 70 } }, ... ] }
// referenceGarmentId 가 null 이면 같은 종류의 기준 옷 중 가장 최근 것을 자동으로 고른다.
public record SizeCheckRequest(
        @NotNull(message = "카테고리를 골라 주세요")
        GarmentCategory category,

        @NotBlank(message = "상품 이름을 입력해 주세요")
        @Size(max = 100, message = "상품 이름은 100자 이내로 입력해 주세요")
        String productName,

        @Size(max = 500, message = "상품 링크가 너무 길어요")
        String productUrl,

        Long referenceGarmentId,

        @NotEmpty(message = "사이즈를 하나 이상 입력해 주세요")
        @Size(max = 10, message = "사이즈는 10개까지 입력할 수 있어요")
        List<@Valid SizeInput> sizes
) {

    public record SizeInput(
            @NotBlank(message = "사이즈 이름을 입력해 주세요")
            @Size(max = 20, message = "사이즈 이름은 20자 이내로 입력해 주세요")
            String label,

            @NotNull(message = "실측값을 입력해 주세요")
            Map<MeasurementPart, Double> measurements
    ) {
    }
}
