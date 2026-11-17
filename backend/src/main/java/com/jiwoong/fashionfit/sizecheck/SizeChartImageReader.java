package com.jiwoong.fashionfit.sizecheck;

import com.jiwoong.fashionfit.garment.GarmentCategory;

// 실측표 이미지 → SizeChartReading. LLM 제공사는 구현체가 정한다 (테스트에서는 가짜 구현 사용).
public interface SizeChartImageReader {

    SizeChartReading read(byte[] image, String contentType, GarmentCategory category);
}
