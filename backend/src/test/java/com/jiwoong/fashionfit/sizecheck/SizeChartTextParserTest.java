package com.jiwoong.fashionfit.sizecheck;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jiwoong.fashionfit.garment.MeasurementPart;

// 실제 쇼핑몰에서 볼 수 있는 표기들을 그대로 넣어 본다. 스프링 없이 도는 순수 단위 테스트.
class SizeChartTextParserTest {

    private Map<String, Map<MeasurementPart, Double>> parse(String text) {
        return SizeChartTextParser.parse(text);
    }

    // ---------- ① 한 줄 나열 ----------

    @Test
    void 성공_상품_설명을_통째로_붙여도_사이즈_줄만_읽는다() {
        var sizes = parse("""
                코멘트 휘뚤마뚤 입기 좋은 경량패딩
                - 원단
                폴리에스테르 100%
                - 사이즈(cm)
                M (추천사이즈 95~100) - 어깨 52 / 가슴 58 / 소매 62 / 총장 68
                L (추천사이즈105~110) - 어깨 54 / 가슴 60 / 소매 64 / 총장 70
                모델 176cm, 67kg / 정핏 블랙 / M 사이즈 착용
                판매가 48,000원
                배송비 3,000원 (70,000원 이상 구매 시 무료)
                상품문의 10
                """);

        assertThat(sizes).containsOnlyKeys("M", "L");
        assertThat(sizes.get("M")).containsEntry(SHOULDER_WIDTH, 52.0).containsEntry(CHEST_WIDTH, 58.0)
                .containsEntry(SLEEVE_LENGTH, 62.0).containsEntry(TOTAL_LENGTH, 68.0);
        // 괄호 안의 추천 사이즈(105~110)를 실측으로 읽지 않는다
        assertThat(sizes.get("L")).containsEntry(SHOULDER_WIDTH, 54.0);
    }

    @Test
    void 성공_띄어쓰기_없이_붙여_쓴_값도_읽는다() {
        var sizes = parse("S 총장70 가슴55\nM 총장72 가슴57.5");

        assertThat(sizes.get("S")).containsEntry(TOTAL_LENGTH, 70.0).containsEntry(CHEST_WIDTH, 55.0);
        assertThat(sizes.get("M")).containsEntry(CHEST_WIDTH, 57.5);
    }

    @Test
    void 성공_한_줄에_여러_사이즈가_있으면_나눠_읽는다() {
        var sizes = parse("S : 총장 70cm, 가슴 55cm / M : 총장 72cm, 가슴 57cm");

        assertThat(sizes).containsOnlyKeys("S", "M");
        assertThat(sizes.get("M")).containsEntry(TOTAL_LENGTH, 72.0);
    }

    @Test
    void 성공_사이즈_이름이_윗줄에_따로_있어도_읽는다() {
        var sizes = parse("M\n어깨 52\n가슴 58\nL\n어깨 54\n가슴 60");

        assertThat(sizes.get("M")).containsEntry(SHOULDER_WIDTH, 52.0).containsEntry(CHEST_WIDTH, 58.0);
        assertThat(sizes.get("L")).containsEntry(CHEST_WIDTH, 60.0);
    }

    @Test
    void 성공_사이즈_이름이_없으면_FREE_로_읽는다() {
        var sizes = parse("어깨 52 / 가슴 58 / 소매기장 62 / 총기장 68");

        assertThat(sizes).containsOnlyKeys("FREE");
        assertThat(sizes.get("FREE")).containsEntry(SLEEVE_LENGTH, 62.0).containsEntry(TOTAL_LENGTH, 68.0);
    }

    @Test
    void 성공_둘레는_반으로_나눠_단면으로_바꾸고_뒷밑위는_버린다() {
        var sizes = parse("28 - 허리둘레 76 / 엉덩이둘레 100 / 허벅지 30 / 밑위 28 / 뒷밑위 38 / 밑단 20 / 총장 102");

        assertThat(sizes.get("28")).containsEntry(WAIST_WIDTH, 38.0).containsEntry(HIP_WIDTH, 50.0)
                .containsEntry(THIGH_WIDTH, 30.0).containsEntry(RISE, 28.0).containsEntry(HEM_WIDTH, 20.0);
    }

    @Test
    void 성공_인치는_cm_로_바꾼다() {
        var sizes = parse("SIZE(inch)\nM - chest 22 / length 27 / sleeve length 24");

        assertThat(sizes.get("M").get(CHEST_WIDTH)).isCloseTo(55.88, within(0.01));
        assertThat(sizes.get("M").get(SLEEVE_LENGTH)).isCloseTo(60.96, within(0.01));
    }

    @Test
    void 성공_소매통은_소매길이로_읽지_않는다() {
        var sizes = parse("M 소매통 20 소매 62");

        assertThat(sizes.get("M")).containsOnly(Map.entry(SLEEVE_LENGTH, 62.0));
    }

    @Test
    void 성공_콜론_cm_괄호가_섞여_있어도_읽는다() {
        var sizes = parse("총장: 68, 어깨 52cm, 소매(62), 가슴단면(58cm)");

        assertThat(sizes.get("FREE")).containsEntry(TOTAL_LENGTH, 68.0).containsEntry(SHOULDER_WIDTH, 52.0)
                .containsEntry(SLEEVE_LENGTH, 62.0).containsEntry(CHEST_WIDTH, 58.0);
    }

    @Test
    void 성공_사이즈_옆_괄호_숫자는_실측이_아니고_둘레_괄호값은_반으로_나눈다() {
        var sizes = parse("M(95) 어깨 52 가슴둘레(116)");

        assertThat(sizes).containsOnlyKeys("M");
        assertThat(sizes.get("M")).containsEntry(CHEST_WIDTH, 58.0);
    }

    @Test
    void 성공_모르는_항목이_섞여_있어도_오류_없이_아는_부위만_읽는다() {
        // "어깨무관 70"은 어깨 실측이 아니고, 암홀은 우리가 쓰지 않는 부위다
        var sizes = parse("F : 어깨무관 70 / 암홀 25cm / 가슴 60");

        assertThat(sizes).containsOnlyKeys("F");
        assertThat(sizes.get("F")).containsOnly(Map.entry(CHEST_WIDTH, 60.0));
    }

    @Test
    void 성공_전체둘레도_반으로_나눈다() {
        assertThat(parse("가슴 전체둘레 116").get("FREE")).containsEntry(CHEST_WIDTH, 58.0);
    }

    // ---------- ② 표 ----------

    @Test
    void 성공_탭으로_나뉜_표를_읽는다() {
        var sizes = parse("사이즈\t총장\t어깨너비\t가슴단면\t소매길이\nM\t68\t52\t58\t62\nL\t70\t54\t60\t64");

        assertThat(sizes).containsOnlyKeys("M", "L");
        assertThat(sizes.get("L")).containsEntry(TOTAL_LENGTH, 70.0).containsEntry(SLEEVE_LENGTH, 64.0);
    }

    @Test
    void 성공_머리줄에_사이즈_칸이_없는_표도_읽는다() {
        var sizes = parse("cm 총장 어깨너비 가슴단면 소매길이\nMY\nS 66 44 49 60\nM 68 46 51.5 61");

        assertThat(sizes).containsOnlyKeys("S", "M");
        assertThat(sizes.get("M")).containsEntry(CHEST_WIDTH, 51.5);
    }

    @Test
    void 성공_안_쓰는_칸이_끼어_있어도_자리가_밀리지_않는다() {
        var sizes = parse("사이즈 총장 가슴 단면 소매통 소매 길이\nM 68 58 20 62");

        assertThat(sizes.get("M")).containsEntry(CHEST_WIDTH, 58.0).containsEntry(SLEEVE_LENGTH, 62.0);
    }

    // ---------- ③ 뒤집힌 표 ----------

    @Test
    void 성공_부위가_줄이고_사이즈가_칸인_표를_읽는다() {
        var sizes = parse("사이즈 S M L\n총장 68 70 72\n가슴단면 55 57.5 60\n소매통 20 21 22");

        assertThat(sizes).containsOnlyKeys("S", "M", "L");
        assertThat(sizes.get("M")).containsOnly(Map.entry(TOTAL_LENGTH, 70.0), Map.entry(CHEST_WIDTH, 57.5));
    }

    // ---------- 실패 ----------

    @Test
    void 실패_실측이_없는_글이면_빈_결과() {
        assertThat(parse("배송비 3,000원\n상품문의 10\n사용후기 3")).isEmpty();
    }

    @Test
    void 실패_비어_있거나_null_이면_빈_결과() {
        assertThat(parse("   ")).isEmpty();
        assertThat(parse(null)).isEmpty();
    }
}
