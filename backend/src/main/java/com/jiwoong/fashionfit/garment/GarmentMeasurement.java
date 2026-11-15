package com.jiwoong.fashionfit.garment;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD의 garment_measurement. "부위 + 값"을 한 행으로 저장한다.
// 카테고리마다 부위가 달라서, 부위를 컬럼으로 만들면 빈 칸이 많아지고 부위가 늘 때마다 테이블을 바꿔야 한다.
@Entity
@Table(name = "garment_measurement",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reference_garment_id", "part"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GarmentMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_garment_id", nullable = false)
    private ReferenceGarment garment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private MeasurementPart part;

    @Column(nullable = false)
    private Double valueCm;

    // 같은 패키지(ReferenceGarment)에서만 만든다: 실측은 항상 옷을 통해서만 생기고 지워진다
    GarmentMeasurement(ReferenceGarment garment, MeasurementPart part, Double valueCm) {
        this.garment = garment;
        this.part = part;
        this.valueCm = valueCm;
    }

    void changeValue(Double valueCm) {
        this.valueCm = valueCm;
    }
}
