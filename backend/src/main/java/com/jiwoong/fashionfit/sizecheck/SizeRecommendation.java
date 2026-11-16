package com.jiwoong.fashionfit.sizecheck;

import java.util.List;

import com.jiwoong.fashionfit.garment.MeasurementPart;

// SizeRecommender 의 결과. 그대로 API 응답에도 쓴다.
public record SizeRecommendation(String recommendedSize, Verdict verdict, List<SizeResult> sizes) {

    // 사이즈 하나의 비교 결과. score 가 작을수록 기준 옷과 비슷하다 (비교할 부위가 없으면 null)
    public record SizeResult(String label, Double score, boolean recommended, List<PartDiff> parts) {
    }

    // 부위 하나의 비교. diffCm = 상품 - 기준 옷 (양수면 상품이 더 큼)
    public record PartDiff(MeasurementPart part, String label, Double productCm, Double referenceCm, Double diffCm) {
    }
}
