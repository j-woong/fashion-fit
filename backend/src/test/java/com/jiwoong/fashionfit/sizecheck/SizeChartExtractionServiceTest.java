package com.jiwoong.fashionfit.sizecheck;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jiwoong.fashionfit.analysis.AnalysisFailedException;
import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.sizecheck.SizeChartReading.Row;

// 실제 Gemini 대신 가짜 리더를 넣어서 "읽은 값을 어떻게 거르는지"만 검사한다
class SizeChartExtractionServiceTest {

    private static final byte[] IMAGE = {1, 2, 3};

    private SizeChartExtractionService serviceReturning(SizeChartReading reading) {
        return new SizeChartExtractionService((image, type, category) -> reading);
    }

    @Test
    void 성공_카테고리에_필요한_부위만_남기고_사이즈별로_돌려준다() {
        // 상의 실측표인데 LLM 이 허리단면(40)까지 읽어 온 경우
        SizeChartReading reading = new SizeChartReading(true, List.of(
                new Row("M", 68.0, 47.0, 55.0, 59.0, 40.0, null, null, null, null),
                new Row(" L ", 70.04, 49.0, 57.5, 60.0, null, null, null, null, null)));

        SizeChartExtractionResponse response =
                serviceReturning(reading).extract(IMAGE, "image/png", GarmentCategory.LONG_SLEEVE);

        assertThat(response.sizes()).hasSize(2);
        assertThat(response.sizes().getFirst().measurements())
                .containsOnlyKeys(TOTAL_LENGTH, SHOULDER_WIDTH, CHEST_WIDTH, SLEEVE_LENGTH);
        assertThat(response.sizes().get(1).label()).isEqualTo("L");
        assertThat(response.sizes().get(1).measurements().get(TOTAL_LENGTH)).isEqualTo(70.0);
    }

    @Test
    void 성공_말이_안_되는_값은_빼고_나머지만_채운다() {
        SizeChartReading reading = new SizeChartReading(true, List.of(
                new Row("M", 680.0, 47.0, 0.0, null, null, null, null, null, null)));

        SizeChartExtractionResponse response =
                serviceReturning(reading).extract(IMAGE, "image/png", GarmentCategory.LONG_SLEEVE);

        assertThat(response.sizes().getFirst().measurements()).containsOnlyKeys(SHOULDER_WIDTH);
    }

    @Test
    void 실패_실측표가_아닌_이미지면_알려준다() {
        SizeChartReading notChart = new SizeChartReading(false, List.of());

        assertThatThrownBy(() -> serviceReturning(notChart).extract(IMAGE, "image/png", GarmentCategory.PANTS))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("실측표를 찾지 못했어요");
    }

    @Test
    void 실패_읽은_줄에_쓸_수_있는_값이_하나도_없으면_알려준다() {
        // 바지 카테고리인데 상의 부위만 읽힌 경우
        SizeChartReading reading = new SizeChartReading(true, List.of(
                new Row("M", null, 47.0, 55.0, 59.0, null, null, null, null, null)));

        assertThatThrownBy(() -> serviceReturning(reading).extract(IMAGE, "image/png", GarmentCategory.PANTS))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 성공_글자에서_읽은_값도_카테고리에_맞는_부위만_남긴다() {
        // 상의인데 글에 허리가 같이 적혀 있는 경우. LLM 은 불리면 안 된다.
        SizeChartExtractionService service = new SizeChartExtractionService(
                (image, type, category) -> { throw new IllegalStateException("LLM 을 부르면 안 된다"); });

        SizeChartExtractionResponse response = service.extractFromText(
                "M (추천 95~100) - 어깨 52 / 가슴 58 / 소매 62 / 총장 68 / 허리 50", GarmentCategory.OUTER);

        assertThat(response.sizes()).singleElement().satisfies(row -> {
            assertThat(row.label()).isEqualTo("M");
            assertThat(row.measurements()).containsOnlyKeys(TOTAL_LENGTH, SHOULDER_WIDTH, CHEST_WIDTH, SLEEVE_LENGTH);
        });
    }

    @Test
    void 실패_글자에_실측이_없으면_알려준다() {
        assertThatThrownBy(() -> serviceReturning(null).extractFromText("배송비 3,000원", GarmentCategory.OUTER))
                .isInstanceOfSatisfying(InvalidInputException.class,
                        e -> assertThat(e.getFieldErrors()).containsKey("text"));
    }

    @Test
    void 실패_LLM_호출이_실패하면_AnalysisFailedException() {
        SizeChartExtractionService service = new SizeChartExtractionService(
                (image, type, category) -> { throw new IllegalStateException("429"); });

        assertThatThrownBy(() -> service.extract(IMAGE, "image/png", GarmentCategory.PANTS))
                .isInstanceOf(AnalysisFailedException.class);
    }
}
