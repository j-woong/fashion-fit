package com.jiwoong.fashionfit.sizecheck;

import java.util.List;
import java.util.Map;

import com.jiwoong.fashionfit.garment.MeasurementPart;

// 프론트의 src/api/sizeChecks.ts ExtractedSizeChart 와 짝. 화면의 실측표 입력칸에 그대로 채워진다.
public record SizeChartExtractionResponse(List<SizeRow> sizes) {

    public record SizeRow(String label, Map<MeasurementPart, Double> measurements) {
    }
}
