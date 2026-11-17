package com.jiwoong.fashionfit.analysis;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.jiwoong.fashionfit.common.InvalidInputException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 스크린샷 분석: 입력 검사 → LLM 분석 → 쇼핑몰 검색 링크 생성
// 결과는 저장하지 않는다 (v1). 이미지도 저장하지 않아서 개인 사진이 서버에 남지 않는다.
@Slf4j
@Service
@RequiredArgsConstructor
public class GarmentAnalysisService {

    static final long MAX_BYTES = ImageValidator.MAX_BYTES;

    private final GarmentImageAnalyzer analyzer;
    private final ShoppingLinkBuilder linkBuilder;

    public GarmentAnalysisResponse analyze(byte[] image, String contentType) {
        ImageValidator.validate(image, contentType);

        GarmentAnalysis analysis;
        try {
            analysis = analyzer.analyze(image, contentType);
        } catch (RuntimeException e) {
            log.warn("스크린샷 분석 실패: {}", e.getMessage(), e);
            throw new AnalysisFailedException("분석에 실패했어요. 잠시 후 다시 시도해 주세요.", e);
        }

        if (analysis == null || !analysis.clothing()) {
            throw new InvalidInputException("사진에서 옷을 찾지 못했어요",
                    Map.of("image", "옷이 크게 잘 보이는 스크린샷을 올려 주세요"));
        }
        return GarmentAnalysisResponse.of(analysis, linkBuilder.build(analysis.searchQueries()));
    }
}
