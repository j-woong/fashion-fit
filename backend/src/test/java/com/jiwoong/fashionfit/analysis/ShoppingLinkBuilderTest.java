package com.jiwoong.fashionfit.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jiwoong.fashionfit.analysis.GarmentAnalysisResponse.SearchLinks;

class ShoppingLinkBuilderTest {

    private final ShoppingLinkBuilder builder = new ShoppingLinkBuilder();

    @Test
    void 성공_검색어마다_모든_쇼핑몰_링크를_만들고_한글을_인코딩한다() {
        List<SearchLinks> result = builder.build(List.of("오버핏 니트"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().links()).hasSize(ShoppingMall.values().length);
        assertThat(result.getFirst().links().getFirst().url())
                .startsWith("https://search.shopping.naver.com/search/all?query=")
                .doesNotContain(" ")
                .contains("%EC%98%A4%EB%B2%84%ED%95%8F"); // "오버핏"
    }

    @Test
    void 성공_빈_검색어와_중복은_빼고_최대_3개까지만_쓴다() {
        List<SearchLinks> result = builder.build(
                Arrays.asList("니트", " 니트 ", "", null, "가디건", "스웨터", "맨투맨"));

        assertThat(result).extracting(SearchLinks::query).containsExactly("니트", "가디건", "스웨터");
    }

    @Test
    void 실패_검색어가_없으면_빈_목록() {
        assertThat(builder.build(null)).isEmpty();
    }
}
