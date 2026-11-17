package com.jiwoong.fashionfit.analysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 검색 링크를 만들 쇼핑몰. %s 자리에 URL 인코딩된 검색어가 들어간다.
// 공식 상품 검색 API 대신 "검색 결과 페이지 링크"를 주는 방식이라 API 키·약관 문제가 없다.
@Getter
@RequiredArgsConstructor
public enum ShoppingMall {
    NAVER_SHOPPING("네이버쇼핑", "https://search.shopping.naver.com/search/all?query=%s"),
    MUSINSA("무신사", "https://www.musinsa.com/search/goods?keyword=%s"),
    COUPANG("쿠팡", "https://www.coupang.com/np/search?q=%s"),
    GOOGLE_SHOPPING("구글 쇼핑", "https://www.google.com/search?tbm=shop&q=%s");

    private final String label;
    private final String searchUrlTemplate;
}
