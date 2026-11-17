package com.jiwoong.fashionfit.sizecheck;

import java.time.LocalDateTime;
import java.util.List;

import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.sizecheck.SizeChartExtractionResponse.SizeRow;

// 전에 같은 상품 링크로 저장해 둔 실측표. 프론트의 src/api/sizeChecks.ts SavedSizeChart 와 짝.
// savedAt: 오래된 실측표일 수 있어서(상품 수정·리뉴얼) 언제 저장한 값인지 화면에 같이 보여준다.
public record SavedSizeChartResponse(Long sizeCheckId, GarmentCategory category, String productName,
                                     LocalDateTime savedAt, List<SizeRow> sizes) {

    static SavedSizeChartResponse from(SizeChart chart) {
        List<SizeRow> sizes = chart.getSizes().entrySet().stream()
                .map(entry -> new SizeRow(entry.getKey(), entry.getValue()))
                .toList();
        return new SavedSizeChartResponse(chart.getId(), chart.getCategory(), chart.getProductName(),
                chart.getCreatedAt(), sizes);
    }
}
