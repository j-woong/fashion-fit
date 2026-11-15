package com.jiwoong.fashionfit.garment;

import java.util.Arrays;
import java.util.List;

// 등록 화면을 그리는 데 필요한 선택지: 카테고리별 실측 부위(+재는 법), 핏 느낌.
// enum 이 바뀌면 화면도 자동으로 따라간다.
public record GarmentOptionsResponse(List<CategoryOption> categories, List<Option> fitFeels) {

    public record CategoryOption(GarmentCategory code, String label, List<PartOption> parts) {
    }

    public record PartOption(MeasurementPart code, String label, String guide) {
    }

    public record Option(String code, String label) {
    }

    public static GarmentOptionsResponse create() {
        List<CategoryOption> categories = Arrays.stream(GarmentCategory.values())
                .map(c -> new CategoryOption(c, c.getLabel(), c.getParts().stream()
                        .map(p -> new PartOption(p, p.getLabel(), p.getGuide()))
                        .toList()))
                .toList();
        List<Option> fitFeels = Arrays.stream(FitFeel.values())
                .map(f -> new Option(f.name(), f.getLabel()))
                .toList();
        return new GarmentOptionsResponse(categories, fitFeels);
    }
}
