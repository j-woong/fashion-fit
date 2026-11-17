package com.jiwoong.fashionfit.analysis;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

// Spring AI ChatClient 로 Gemini 에 이미지를 보내고, 답을 GarmentAnalysis 로 받는다.
// 모델·키는 application.yml 의 spring.ai.google.genai 설정을 따른다.
@Component
public class GeminiGarmentImageAnalyzer implements GarmentImageAnalyzer {

    private static final String SYSTEM_PROMPT = """
            너는 패션 쇼핑을 돕는 분석가다. 사용자가 올린 옷 스크린샷을 보고 한국어로 답한다.
            규칙:
            - 사진에서 보이는 것만 말한다. 브랜드, 가격, 품질 등급은 추측하지 않는다.
            - 사진에 옷이 없으면 clothing 을 false 로 하고 나머지는 비운다.
            - 여러 벌이 있으면 가장 크게 보이는 한 벌만 분석한다.
            - category 는 SHORT_SLEEVE(반팔 상의), LONG_SLEEVE(긴팔 상의: 맨투맨·니트·셔츠·후드), OUTER(아우터), PANTS(바지) 중 하나.
              치마·원피스·신발·가방·액세서리처럼 해당하는 게 없으면 null.
            - materialGuess 는 "울 혼방으로 보임"처럼 추정이라는 게 드러나게 쓴다.
            - textureGuess 는 사진에서 보이는 질감으로 촉감을 추정한다. "부드럽고 포근해 보임", "약간 거칠고 빳빳해 보임",
              "매끈하고 바스락거려 보임"처럼 쓰고, 두께감(얇음·도톰함)도 보이면 함께 쓴다.
            - materialKeywords 는 비슷한 옷을 찾을 때 검색 필터로 쓸 소재 단어 2~4개. 예: ["울", "캐시미어", "아크릴"]
            - searchQueries 는 한국 쇼핑몰에서 비슷한 옷을 찾을 검색어 2~3개. 핏·소재·디테일·색을 조합하고 브랜드명은 넣지 않는다.
              예: "오버핏 크루넥 울 니트 그레이", "드롭숄더 니트 회색"
            - qualityChecks 는 이 종류의 옷을 살 때 상품 페이지에서 확인할 점 3~5개. 판정이 아니라 확인 방법을 쓴다.
              예: "혼용률에서 울 비율 확인 (30% 이상이면 보온성이 좋음)", "리뷰 사진에서 보풀 여부 확인"
            """;

    private static final String USER_PROMPT = "이 옷을 분석해 줘.";

    private final ChatClient chatClient;

    public GeminiGarmentImageAnalyzer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public GarmentAnalysis analyze(byte[] image, String contentType) {
        return chatClient.prompt()
                .user(user -> user
                        .text(USER_PROMPT)
                        .media(MimeTypeUtils.parseMimeType(contentType), new ByteArrayResource(image)))
                .call()
                .entity(GarmentAnalysis.class); // 응답 JSON → record (형식이 안 맞으면 예외)
    }
}
