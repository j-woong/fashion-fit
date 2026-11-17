package com.jiwoong.fashionfit.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.garment.GarmentCategory;

// 실제 Gemini 대신 가짜 분석기를 넣어서 서비스 규칙만 검사한다 (인터페이스로 분리한 덕분)
class GarmentAnalysisServiceTest {

    private static final byte[] IMAGE = {1, 2, 3};

    private static final GarmentAnalysis KNIT = new GarmentAnalysis(true, GarmentCategory.LONG_SLEEVE,
            "오버핏 크루넥 니트", "회색 오버핏 니트", "그레이", "오버핏", "울 혼방으로 보임",
            "부드럽고 포근해 보임", List.of("울", "아크릴"),
            List.of("드롭숄더"), List.of("오버핏 크루넥 니트 그레이", "드롭숄더 니트"), List.of("혼용률 확인"));

    private GarmentAnalysisService serviceReturning(GarmentAnalysis result) {
        return new GarmentAnalysisService((image, type) -> result, new ShoppingLinkBuilder());
    }

    @Test
    void 성공_분석_결과와_검색어별_쇼핑몰_링크를_돌려준다() {
        GarmentAnalysisResponse response = serviceReturning(KNIT).analyze(IMAGE, "image/png");

        assertThat(response.itemName()).isEqualTo("오버핏 크루넥 니트");
        assertThat(response.categoryLabel()).isEqualTo(GarmentCategory.LONG_SLEEVE.getLabel());
        assertThat(response.searches()).hasSize(2);
        assertThat(response.textureGuess()).isEqualTo("부드럽고 포근해 보임");
        assertThat(response.materialKeywords()).containsExactly("울", "아크릴");
    }

    @Test
    void 실패_이미지가_아닌_파일은_분석하지_않는다() {
        assertThatThrownBy(() -> serviceReturning(KNIT).analyze(IMAGE, "application/pdf"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 실패_5MB가_넘으면_분석하지_않는다() {
        byte[] big = new byte[(int) GarmentAnalysisService.MAX_BYTES + 1];

        assertThatThrownBy(() -> serviceReturning(KNIT).analyze(big, "image/jpeg"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 실패_사진에_옷이_없으면_알려준다() {
        GarmentAnalysis notClothing = new GarmentAnalysis(false, null, null, null, null, null, null,
                null, null, null, null, null);

        assertThatThrownBy(() -> serviceReturning(notClothing).analyze(IMAGE, "image/png"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("옷을 찾지 못했어요");
    }

    @Test
    void 실패_LLM_호출이_실패하면_AnalysisFailedException() {
        GarmentAnalysisService service = new GarmentAnalysisService(
                (image, type) -> { throw new IllegalStateException("429 Too Many Requests"); },
                new ShoppingLinkBuilder());

        assertThatThrownBy(() -> service.analyze(IMAGE, "image/png"))
                .isInstanceOf(AnalysisFailedException.class);
    }
}
