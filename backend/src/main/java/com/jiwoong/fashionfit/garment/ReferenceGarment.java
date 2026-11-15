package com.jiwoong.fashionfit.garment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.jiwoong.fashionfit.user.User;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD의 reference_garment(기준 옷). "내가 가진, 잘 맞는 옷".
// 실측(GarmentMeasurement)은 이 옷에 딸린 값이라 옷을 저장/삭제하면 같이 저장/삭제된다 (cascade + orphanRemoval).
@Entity
@Table(name = "reference_garment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReferenceGarment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private GarmentCategory category;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private FitFeel fitFeel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MeasurementSource source;

    @OneToMany(mappedBy = "garment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GarmentMeasurement> measurements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private ReferenceGarment(User user) {
        this.user = user;
        this.source = MeasurementSource.MANUAL;
        this.createdAt = LocalDateTime.now();
    }

    // values 는 GarmentService 에서 "이 카테고리에 필요한 부위가 다 있는지" 검사를 마친 값
    public static ReferenceGarment create(User user, GarmentCategory category, String name, FitFeel fitFeel,
                                          Map<MeasurementPart, Double> values) {
        ReferenceGarment garment = new ReferenceGarment(user);
        garment.update(category, name, fitFeel, values);
        return garment;
    }

    public void update(GarmentCategory category, String name, FitFeel fitFeel, Map<MeasurementPart, Double> values) {
        this.category = category;
        this.name = name;
        this.fitFeel = fitFeel;
        applyMeasurements(values);
        this.updatedAt = LocalDateTime.now();
    }

    // 실측을 "전부 지우고 새로 넣기"로 하지 않는 이유:
    // Hibernate 는 flush 할 때 INSERT 를 DELETE 보다 먼저 실행해서, 같은 부위를 지우고 다시 넣으면
    // (reference_garment_id, part) 유니크 제약에 걸린다. 그래서 있는 건 값만 바꾸고, 없어진 것만 지우고, 새것만 추가한다.
    private void applyMeasurements(Map<MeasurementPart, Double> values) {
        measurements.removeIf(m -> !values.containsKey(m.getPart()));
        values.forEach((part, value) -> measurements.stream()
                .filter(m -> m.getPart() == part)
                .findFirst()
                .ifPresentOrElse(
                        m -> m.changeValue(value),
                        () -> measurements.add(new GarmentMeasurement(this, part, value))));
    }

    // 화면에 보여줄 때는 카테고리에 정의된 부위 순서대로 (총장 → 어깨 → 가슴 → 소매)
    public List<GarmentMeasurement> getMeasurementsInOrder() {
        List<MeasurementPart> order = category.getParts();
        return measurements.stream()
                .sorted(Comparator.comparingInt(m -> order.indexOf(m.getPart())))
                .toList();
    }
}
