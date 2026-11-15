package com.jiwoong.fashionfit.garment;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 등록(POST)과 수정(PUT)에 같이 쓴다.
// measurements 예: { "TOTAL_LENGTH": 70, "CHEST_WIDTH": 58.5 }
// "이 카테고리에 맞는 부위가 다 들어왔는지"는 카테고리에 따라 달라서 GarmentService 에서 검사한다.
public record GarmentRequest(
        @NotNull(message = "카테고리를 골라 주세요")
        GarmentCategory category,

        @NotBlank(message = "옷 이름을 입력해 주세요")
        @Size(max = 50, message = "옷 이름은 50자 이내로 입력해 주세요")
        String name,

        @NotNull(message = "핏 느낌을 골라 주세요")
        FitFeel fitFeel,

        @NotNull(message = "실측값을 입력해 주세요")
        Map<MeasurementPart, Double> measurements
) {
}
