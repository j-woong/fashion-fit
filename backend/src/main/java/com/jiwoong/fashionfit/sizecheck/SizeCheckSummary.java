package com.jiwoong.fashionfit.sizecheck;

import java.time.LocalDateTime;

// 비교 기록 목록의 한 줄. 저장해 둔 요약 컬럼만 쓴다.
public record SizeCheckSummary(
        Long id,
        String productName,
        String categoryLabel,
        String recommendedSize,
        String referenceGarmentName,
        LocalDateTime createdAt
) {

    static SizeCheckSummary from(SizeChart chart) {
        return new SizeCheckSummary(chart.getId(), chart.getProductName(), chart.getCategory().getLabel(),
                chart.getRecommendedSize(), chart.getReferenceGarmentName(), chart.getCreatedAt());
    }
}
