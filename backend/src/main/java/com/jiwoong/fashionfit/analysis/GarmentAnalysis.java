package com.jiwoong.fashionfit.analysis;

import java.util.List;

import com.jiwoong.fashionfit.garment.GarmentCategory;

/**
 * LLM 이 스크린샷을 보고 채워 주는 결과. Spring AI 가 이 record 모양대로 JSON 을 받아 객체로 바꿔 준다.
 * 사진에서 "보이는 것"만 담는다. 가격이나 품질 판정은 근거가 없어서 받지 않는다.
 */
public record GarmentAnalysis(
        boolean clothing,              // 사진에 옷이 있는지. 없으면 나머지는 비어 있다
        GarmentCategory category,      // 사이즈 비교 카테고리. 신발·가방 등 해당 없으면 null
        String itemName,               // 짧은 상품명 (예: 오버핏 크루넥 니트)
        String summary,                // 한두 문장 설명
        String color,
        String fit,
        String materialGuess,          // 사진으로 추정한 소재 (확정 아님)
        String textureGuess,           // 사진으로 추정한 촉감·질감 (예: 부드럽고 포근해 보임)
        List<String> materialKeywords, // 검색 필터로 쓸 소재 단어 (예: 울, 캐시미어)
        List<String> details,          // 눈에 띄는 디테일 (예: 드롭숄더, 투웨이 지퍼)
        List<String> searchQueries,    // 쇼핑몰 검색어 2~3개
        List<String> qualityChecks     // 살 때 확인할 점 (판정이 아니라 체크리스트)
) {
}
