package com.jiwoong.fashionfit.sizecheck;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.jiwoong.fashionfit.garment.MeasurementPart;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 실측표의 칸 하나. 자기 id 가 없는 "값 객체"라서 엔티티가 아니라 @Embeddable 로 만들었다.
// (기준 옷의 GarmentMeasurement 는 값만 바꾸는 수정이 있어서 엔티티로 만들었고,
//  실측표는 한 번 저장하면 바꾸지 않아서 @ElementCollection 으로 충분하다)
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SizeChartEntry {

    @Column(name = "size_label", nullable = false, length = 20)
    private String sizeLabel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private MeasurementPart part;

    @Column(name = "value_cm", nullable = false)
    private Double valueCm;
}
