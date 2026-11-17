package com.jiwoong.fashionfit.sizecheck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.jiwoong.fashionfit.garment.MeasurementPart;
import com.jiwoong.fashionfit.sizecheck.SizeRecommendation.PartDiff;
import com.jiwoong.fashionfit.sizecheck.SizeRecommendation.SizeResult;

/**
 * 사이즈 추천 계산. 이 서비스의 핵심 규칙이 모두 여기 있다.
 *
 * <p>방식: 사이즈마다 "기준 옷과 부위별 차이(cm)의 가중 평균"을 구하고, 가장 작은 사이즈를 추천한다.
 * <ul>
 *   <li>가중치: 핏을 크게 좌우하는 부위(가슴단면·허리단면)를 더 중요하게 본다.</li>
 *   <li>상품 실측표와 기준 옷에 둘 다 있는 부위만 비교한다. (쇼핑몰마다 표기하는 부위가 달라서)</li>
 *   <li>계산은 코드로 한다. LLM 에 맡기면 같은 입력에도 결과가 흔들리고, 왜 그 결과인지 설명할 수 없다.</li>
 * </ul>
 *
 * <p>DB, 스프링에 의존하지 않아서 단위 테스트로 규칙을 바로 검증할 수 있다 (SizeRecommenderTest).
 */
@Component
public class SizeRecommender {

    static final double GOOD_SCORE = 1.5;      // 평균 1.5cm 이내: 거의 같음
    static final double SLIGHTLY_OFF_SCORE = 3.0; // 평균 3cm 이내: 조금 차이

    // 핏(붙는지 넉넉한지)을 좌우하는 "품" 부위. 총장·소매·밑위 같은 길이는 핏 판정에서 뺀다.
    private static final Set<MeasurementPart> WIDTH_PARTS = Set.of(
            MeasurementPart.CHEST_WIDTH, MeasurementPart.SHOULDER_WIDTH,
            MeasurementPart.WAIST_WIDTH, MeasurementPart.HIP_WIDTH, MeasurementPart.THIGH_WIDTH);

    private static final Map<MeasurementPart, Double> WEIGHTS = Map.of(
            MeasurementPart.CHEST_WIDTH, 3.0,
            MeasurementPart.SHOULDER_WIDTH, 2.0,
            MeasurementPart.TOTAL_LENGTH, 1.5,
            MeasurementPart.SLEEVE_LENGTH, 1.0,
            MeasurementPart.WAIST_WIDTH, 3.0,
            MeasurementPart.HIP_WIDTH, 1.5,
            MeasurementPart.THIGH_WIDTH, 1.5,
            MeasurementPart.RISE, 1.0,
            MeasurementPart.HEM_WIDTH, 0.5);

    /**
     * @param reference 기준 옷 실측. 기준 옷이 없으면 null
     * @param sizes     상품 실측표. { "M": {CHEST_WIDTH: 56, ...}, ... } (입력 순서 유지)
     */
    public SizeRecommendation recommend(Map<MeasurementPart, Double> reference,
                                        Map<String, Map<MeasurementPart, Double>> sizes) {
        if (reference == null || reference.isEmpty()) {
            return new SizeRecommendation(null, Verdict.NO_REFERENCE, List.of());
        }

        List<SizeResult> results = new ArrayList<>();
        sizes.forEach((label, measurements) -> results.add(compare(label, measurements, reference)));

        SizeResult best = results.stream()
                .filter(r -> r.score() != null)
                .min(Comparator.comparingDouble(SizeResult::score)) // 점수가 같으면 먼저 입력한(작은) 사이즈
                .orElse(null);
        if (best == null) {
            return new SizeRecommendation(null, Verdict.NOT_COMPARABLE, results);
        }

        List<SizeResult> marked = results.stream()
                .map(r -> r == best ? r.markRecommended() : r)
                .toList();
        return new SizeRecommendation(best.label(), verdictOf(best.score()), marked);
    }

    private SizeResult compare(String label, Map<MeasurementPart, Double> product,
                               Map<MeasurementPart, Double> reference) {
        List<PartDiff> parts = new ArrayList<>();
        double weightedSum = 0;
        double weightTotal = 0;
        double widthSignedSum = 0; // 품 부위의 (상품 - 내 사이즈) 합: 핏 판정용
        double widthWeightTotal = 0;

        // 부위 순서는 MeasurementPart 정의 순서(총장 → 어깨 → 가슴 ...)로 고정
        for (MeasurementPart part : MeasurementPart.values()) {
            Double productCm = product.get(part);
            Double referenceCm = reference.get(part);
            if (productCm == null || referenceCm == null) {
                continue;
            }
            double diff = round(productCm - referenceCm);
            double weight = WEIGHTS.getOrDefault(part, 1.0);
            weightedSum += weight * Math.abs(diff);
            weightTotal += weight;
            if (WIDTH_PARTS.contains(part)) {
                widthSignedSum += weight * diff;
                widthWeightTotal += weight;
            }
            parts.add(new PartDiff(part, part.getLabel(), productCm, referenceCm, diff));
        }

        Double score = weightTotal == 0 ? null : round(weightedSum / weightTotal);
        FitType fitType = widthWeightTotal == 0 ? null : FitType.of(widthSignedSum / widthWeightTotal);
        return new SizeResult(label, score, false, fitType,
                fitType == null ? null : fitType.getLabel(),
                fitType == null ? null : fitType.getDescription(),
                parts);
    }

    private Verdict verdictOf(double score) {
        if (score <= GOOD_SCORE) {
            return Verdict.GOOD;
        }
        if (score <= SLIGHTLY_OFF_SCORE) {
            return Verdict.SLIGHTLY_OFF;
        }
        return Verdict.FAR;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
