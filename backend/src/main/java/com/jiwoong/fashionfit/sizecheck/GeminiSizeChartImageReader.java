package com.jiwoong.fashionfit.sizecheck;

import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.MeasurementPart;

@Component
public class GeminiSizeChartImageReader implements SizeChartImageReader {

    private static final String SYSTEM_PROMPT = """
            너는 쇼핑몰 상품 페이지의 실측표(사이즈표) 이미지를 읽어 숫자를 옮겨 적는 도우미다.
            규칙:
            - 이미지에 실측표가 없으면 sizeChart 를 false 로 하고 rows 는 비운다.
            - 표의 사이즈 한 줄마다 row 하나. label 은 표에 적힌 사이즈 이름 그대로 (S, M, L, FREE, 95, 100, 30 등).
            - 숫자는 표에 적힌 그대로 옮긴다. 추측하거나 계산해서 채우지 않는다. 표에 없는 부위는 null.
            - 단위는 cm. 표가 인치면 2.54 를 곱해 cm 로 바꾼다.
            - 열 이름 매핑:
              총장·기장·총기장 → totalLength / 어깨너비·어깨 → shoulderWidth / 가슴단면·가슴·품 → chestWidth /
              소매길이·소매·팔길이 → sleeveLength / 허리단면·허리 → waistWidth / 엉덩이단면·엉덩이·힙 → hipWidth /
              허벅지단면·허벅지 → thighWidth / 밑위 → rise / 밑단단면·밑단 → hemWidth
            - "둘레"로 표기된 값(가슴둘레, 허리둘레 등)은 2 로 나눠 단면으로 바꾼다.
            - 화장(목 중심~소매 끝)처럼 위 목록에 없는 열은 무시한다.
            """;

    private final ChatClient chatClient;

    public GeminiSizeChartImageReader(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public SizeChartReading read(byte[] image, String contentType, GarmentCategory category) {
        String parts = category.getParts().stream()
                .map(MeasurementPart::getLabel)
                .collect(Collectors.joining(", "));
        String userText = "이 실측표를 읽어 줘. 옷 종류는 %s 이고, 필요한 부위는 %s 야."
                .formatted(category.getLabel(), parts);

        return chatClient.prompt()
                .user(user -> user
                        .text(userText)
                        .media(MimeTypeUtils.parseMimeType(contentType), new ByteArrayResource(image)))
                .call()
                .entity(SizeChartReading.class);
    }
}
