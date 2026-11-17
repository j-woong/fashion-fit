package com.jiwoong.fashionfit.sizecheck;

import java.util.List;

/**
 * LLM 이 실측표 이미지를 읽은 결과. 표의 한 줄(사이즈 하나)이 Row 하나.
 * 부위를 Map 이 아니라 고정 필드로 둔 이유: LLM 에게 넘기는 JSON 스키마가 명확해져서 엉뚱한 키를 만들지 않는다.
 * 표에 없는 부위는 null.
 */
public record SizeChartReading(boolean sizeChart, List<Row> rows) {

    public record Row(
            String label,
            Double totalLength,
            Double shoulderWidth,
            Double chestWidth,
            Double sleeveLength,
            Double waistWidth,
            Double hipWidth,
            Double thighWidth,
            Double rise,
            Double hemWidth
    ) {
    }
}
