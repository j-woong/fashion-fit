package com.jiwoong.fashionfit.sizecheck;

import java.time.LocalDateTime;
import java.util.List;

import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.ReferenceGarment;
import com.jiwoong.fashionfit.sizecheck.SizeRecommendation.SizeResult;

// 프론트의 src/api/sizeChecks.ts SizeCheck 타입과 짝
public record SizeCheckResponse(
        Long id,
        GarmentCategory category,
        String categoryLabel,
        String productName,
        String productUrl,
        Reference reference,          // 비교에 쓴 기준 옷 (없으면 null)
        String recommendedSize,       // 추천 사이즈 (비교 불가면 null)
        Verdict verdict,
        String verdictLabel,
        List<SizeResult> sizes,
        LocalDateTime createdAt
) {

    public record Reference(Long id, String name, String fitFeelLabel) {

        static Reference from(ReferenceGarment garment) {
            return garment == null ? null
                    : new Reference(garment.getId(), garment.getName(), garment.getFitFeel().getLabel());
        }
    }

    public static SizeCheckResponse of(SizeChart chart, ReferenceGarment reference, SizeRecommendation result) {
        return new SizeCheckResponse(
                chart.getId(),
                chart.getCategory(),
                chart.getCategory().getLabel(),
                chart.getProductName(),
                chart.getProductUrl(),
                Reference.from(reference),
                result.recommendedSize(),
                result.verdict(),
                result.verdict().getLabel(),
                result.sizes(),
                chart.getCreatedAt());
    }
}
