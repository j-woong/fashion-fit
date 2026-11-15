package com.jiwoong.fashionfit.garment;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 카테고리마다 재야 하는 부위가 다르다. 이 목록이 "무엇을 입력받을지"의 기준이 된다.
// (프론트도 이 목록을 /api/garment-options 로 받아서 입력칸을 그리기 때문에, 부위를 바꿀 때 여기만 고치면 된다)
@Getter
@RequiredArgsConstructor
public enum GarmentCategory {
    SHORT_SLEEVE("반팔 상의", List.of(TOTAL_LENGTH, SHOULDER_WIDTH, CHEST_WIDTH, SLEEVE_LENGTH)),
    LONG_SLEEVE("긴팔 상의 (맨투맨·니트·셔츠)", List.of(TOTAL_LENGTH, SHOULDER_WIDTH, CHEST_WIDTH, SLEEVE_LENGTH)),
    OUTER("아우터", List.of(TOTAL_LENGTH, SHOULDER_WIDTH, CHEST_WIDTH, SLEEVE_LENGTH)),
    PANTS("바지", List.of(TOTAL_LENGTH, WAIST_WIDTH, HIP_WIDTH, THIGH_WIDTH, RISE, HEM_WIDTH));

    private final String label;
    private final List<MeasurementPart> parts;
}
