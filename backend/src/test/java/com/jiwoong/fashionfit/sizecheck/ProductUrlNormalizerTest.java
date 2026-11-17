package com.jiwoong.fashionfit.sizecheck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductUrlNormalizerTest {

    @Test
    void 성공_추적_파라미터만_떼고_상품_번호는_남긴다() {
        String url = "https://shop.com/product/detail.html?product_no=123&utm_source=ig&utm_medium=social&fbclid=abc#review";

        assertThat(ProductUrlNormalizer.normalize(url)).isEqualTo("https://shop.com/product/detail.html?product_no=123");
    }

    @Test
    void 성공_파라미터가_추적용뿐이면_물음표까지_뗀다() {
        assertThat(ProductUrlNormalizer.normalize(" https://shop.com/goods/55?utm_source=ig "))
                .isEqualTo("https://shop.com/goods/55");
    }

    @Test
    void 성공_비어_있으면_null() {
        assertThat(ProductUrlNormalizer.normalize("  ")).isNull();
        assertThat(ProductUrlNormalizer.normalize(null)).isNull();
    }

    @Test
    void 성공_첫_화면_주소를_구분한다() {
        assertThat(ProductUrlNormalizer.isHomepage("https://shop.com/")).isTrue();
        assertThat(ProductUrlNormalizer.isHomepage("https://shop.com")).isTrue();
        assertThat(ProductUrlNormalizer.isHomepage("https://shop.com/goods/55")).isFalse();
        assertThat(ProductUrlNormalizer.isHomepage("https://shop.com/?product_no=1")).isFalse();
    }
}
