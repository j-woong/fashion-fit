package com.jiwoong.fashionfit.garment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

// 스프링 없이 엔티티 로직만 검사하는 단위 테스트
class ReferenceGarmentTest {

    @Test
    void 수정하면_같은_부위는_값만_바뀌고_객체는_그대로다() {
        ReferenceGarment garment = ReferenceGarment.create(null, GarmentCategory.SHORT_SLEEVE, "흰 반팔", FitFeel.REGULAR,
                Map.of(MeasurementPart.TOTAL_LENGTH, 70.0, MeasurementPart.SHOULDER_WIDTH, 50.0,
                        MeasurementPart.CHEST_WIDTH, 55.0, MeasurementPart.SLEEVE_LENGTH, 22.0));
        GarmentMeasurement chestBefore = find(garment, MeasurementPart.CHEST_WIDTH);

        garment.update(GarmentCategory.SHORT_SLEEVE, "흰 반팔", FitFeel.RELAXED,
                Map.of(MeasurementPart.TOTAL_LENGTH, 71.0, MeasurementPart.SHOULDER_WIDTH, 50.0,
                        MeasurementPart.CHEST_WIDTH, 57.5, MeasurementPart.SLEEVE_LENGTH, 22.0));

        assertThat(garment.getMeasurements()).hasSize(4);
        assertThat(find(garment, MeasurementPart.CHEST_WIDTH)).isSameAs(chestBefore);
        assertThat(chestBefore.getValueCm()).isEqualTo(57.5);
    }

    @Test
    void 카테고리를_바꾸면_필요없는_부위는_빠지고_새_부위가_생긴다() {
        ReferenceGarment garment = ReferenceGarment.create(null, GarmentCategory.SHORT_SLEEVE, "옷", FitFeel.REGULAR,
                Map.of(MeasurementPart.TOTAL_LENGTH, 70.0, MeasurementPart.SHOULDER_WIDTH, 50.0,
                        MeasurementPart.CHEST_WIDTH, 55.0, MeasurementPart.SLEEVE_LENGTH, 22.0));

        garment.update(GarmentCategory.PANTS, "옷", FitFeel.REGULAR,
                Map.of(MeasurementPart.TOTAL_LENGTH, 100.0, MeasurementPart.WAIST_WIDTH, 40.0,
                        MeasurementPart.HIP_WIDTH, 52.0, MeasurementPart.THIGH_WIDTH, 32.0,
                        MeasurementPart.RISE, 30.0, MeasurementPart.HEM_WIDTH, 20.0));

        assertThat(garment.getMeasurementsInOrder())
                .extracting(GarmentMeasurement::getPart)
                .containsExactlyElementsOf(GarmentCategory.PANTS.getParts());
    }

    private GarmentMeasurement find(ReferenceGarment garment, MeasurementPart part) {
        return garment.getMeasurements().stream().filter(m -> m.getPart() == part).findFirst().orElseThrow();
    }
}
