package com.jiwoong.fashionfit.sizecheck;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jiwoong.fashionfit.garment.MeasurementPart;

// 사이즈 추천 규칙 단위 테스트 (스프링·DB 없음)
class SizeRecommenderTest {

    private final SizeRecommender recommender = new SizeRecommender();

    // 기준 옷: 가슴 58, 어깨 50, 총장 70, 소매 60
    private final Map<MeasurementPart, Double> reference = Map.of(
            CHEST_WIDTH, 58.0, SHOULDER_WIDTH, 50.0, TOTAL_LENGTH, 70.0, SLEEVE_LENGTH, 60.0);

    @Test
    void 성공_기준_옷과_가장_비슷한_사이즈를_추천한다() {
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        sizes.put("M", Map.of(CHEST_WIDTH, 55.0, SHOULDER_WIDTH, 47.0, TOTAL_LENGTH, 68.0, SLEEVE_LENGTH, 59.0));
        sizes.put("L", Map.of(CHEST_WIDTH, 58.0, SHOULDER_WIDTH, 50.0, TOTAL_LENGTH, 71.0, SLEEVE_LENGTH, 60.0));
        sizes.put("XL", Map.of(CHEST_WIDTH, 61.0, SHOULDER_WIDTH, 53.0, TOTAL_LENGTH, 74.0, SLEEVE_LENGTH, 61.0));

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.recommendedSize()).isEqualTo("L");
        assertThat(result.verdict()).isEqualTo(Verdict.GOOD);
        assertThat(result.sizes()).extracting(SizeRecommendation.SizeResult::recommended)
                .containsExactly(false, true, false);
    }

    @Test
    void 성공_가슴단면이_총장보다_중요하게_반영된다() {
        // A: 가슴 0cm 차이, 총장 4cm 차이  /  B: 가슴 4cm 차이, 총장 0cm 차이
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        sizes.put("A", Map.of(CHEST_WIDTH, 58.0, TOTAL_LENGTH, 74.0));
        sizes.put("B", Map.of(CHEST_WIDTH, 62.0, TOTAL_LENGTH, 70.0));

        assertThat(recommender.recommend(reference, sizes).recommendedSize()).isEqualTo("A");
    }

    @Test
    void 성공_실측표에_일부_부위만_있어도_있는_부위로_비교한다() {
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        sizes.put("FREE", Map.of(CHEST_WIDTH, 59.0));

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.recommendedSize()).isEqualTo("FREE");
        assertThat(result.sizes().getFirst().parts()).hasSize(1);
        assertThat(result.sizes().getFirst().parts().getFirst().diffCm()).isEqualTo(1.0);
    }

    @Test
    void 실패_모든_사이즈가_기준_옷과_크게_다르면_FAR() {
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        sizes.put("S", Map.of(CHEST_WIDTH, 48.0, SHOULDER_WIDTH, 42.0));

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.verdict()).isEqualTo(Verdict.FAR);
        assertThat(result.recommendedSize()).isEqualTo("S"); // 그래도 가장 가까운 사이즈는 알려준다
    }

    @Test
    void 실패_기준_옷이_없으면_추천하지_않는다() {
        Map<String, Map<MeasurementPart, Double>> sizes = Map.of("M", Map.of(CHEST_WIDTH, 58.0));

        SizeRecommendation result = recommender.recommend(null, sizes);

        assertThat(result.verdict()).isEqualTo(Verdict.NO_REFERENCE);
        assertThat(result.recommendedSize()).isNull();
    }

    @Test
    void 실패_기준_옷과_겹치는_부위가_없으면_비교할_수_없다() {
        Map<String, Map<MeasurementPart, Double>> sizes = Map.of("30", Map.of(WAIST_WIDTH, 40.0));

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.verdict()).isEqualTo(Verdict.NOT_COMPARABLE);
        assertThat(result.recommendedSize()).isNull();
    }

    @Test
    void 성공_품_차이로_내_사이즈_대비_핏을_알려준다() {
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        sizes.put("S", Map.of(CHEST_WIDTH, 53.0, SHOULDER_WIDTH, 45.0));  // 품 -5cm
        sizes.put("M", Map.of(CHEST_WIDTH, 58.0, SHOULDER_WIDTH, 50.0));  // 같음
        sizes.put("L", Map.of(CHEST_WIDTH, 61.0, SHOULDER_WIDTH, 53.0));  // 품 +3cm
        sizes.put("XL", Map.of(CHEST_WIDTH, 65.0, SHOULDER_WIDTH, 56.0)); // 품 +6~7cm

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.sizes()).extracting(SizeRecommendation.SizeResult::fitType)
                .containsExactly(FitType.TIGHT, FitType.REGULAR, FitType.SEMI_OVER, FitType.OVER);
    }

    @Test
    void 실패_품_부위가_없으면_핏을_판정하지_않는다() {
        Map<String, Map<MeasurementPart, Double>> sizes = Map.of("M", Map.of(TOTAL_LENGTH, 70.0));

        SizeRecommendation result = recommender.recommend(reference, sizes);

        assertThat(result.sizes().getFirst().fitType()).isNull();
        assertThat(result.recommendedSize()).isEqualTo("M"); // 길이만으로도 추천은 한다
    }
}
