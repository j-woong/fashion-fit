package com.jiwoong.fashionfit.garment;

import java.util.List;

// 프론트의 src/api/garments.ts 에 있는 Garment 타입과 짝.
// code(CHEST_WIDTH)와 화면용 이름(가슴단면)을 같이 내려줘서 프론트가 한글 이름을 따로 들고 있지 않아도 된다.
public record GarmentResponse(
        Long id,
        GarmentCategory category,
        String categoryLabel,
        String name,
        FitFeel fitFeel,
        String fitFeelLabel,
        List<Measurement> measurements
) {

    public record Measurement(MeasurementPart part, String label, Double valueCm) {
    }

    public static GarmentResponse from(ReferenceGarment garment) {
        List<Measurement> measurements = garment.getMeasurementsInOrder().stream()
                .map(m -> new Measurement(m.getPart(), m.getPart().getLabel(), m.getValueCm()))
                .toList();
        return new GarmentResponse(
                garment.getId(),
                garment.getCategory(),
                garment.getCategory().getLabel(),
                garment.getName(),
                garment.getFitFeel(),
                garment.getFitFeel().getLabel(),
                measurements);
    }
}
