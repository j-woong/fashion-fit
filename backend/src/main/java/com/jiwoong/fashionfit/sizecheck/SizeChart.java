package com.jiwoong.fashionfit.sizecheck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.MeasurementPart;
import com.jiwoong.fashionfit.user.User;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD의 size_chart: 사고 싶은 옷의 실측표 + 비교 결과 요약(추천 사이즈, 기준 옷 이름)
@Entity
@Table(name = "size_chart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SizeChart {

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

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(length = 500)
    private String productUrl;

    private Long referenceGarmentId;

    @Column(length = 50)
    private String referenceGarmentName;

    @Column(length = 20)
    private String recommendedSize;

    @ElementCollection
    @CollectionTable(name = "size_chart_entry", joinColumns = @JoinColumn(name = "size_chart_id"))
    @OrderColumn(name = "entry_order")
    private List<SizeChartEntry> entries = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private SizeChart(User user, GarmentCategory category, String productName, String productUrl,
                      List<SizeChartEntry> entries) {
        this.user = user;
        this.category = category;
        this.productName = productName;
        this.productUrl = productUrl;
        this.entries.addAll(entries);
        this.createdAt = LocalDateTime.now();
    }

    public static SizeChart create(User user, GarmentCategory category, String productName, String productUrl,
                                   List<SizeChartEntry> entries) {
        return new SizeChart(user, category, productName, productUrl, entries);
    }

    // 비교 결과 요약을 기록해 둔다. 목록 화면은 이 값만 읽어서 실측표 전체를 불러오지 않아도 된다.
    public void recordResult(Long referenceGarmentId, String referenceGarmentName, String recommendedSize) {
        this.referenceGarmentId = referenceGarmentId;
        this.referenceGarmentName = referenceGarmentName;
        this.recommendedSize = recommendedSize;
    }

    // 평평하게 저장된 칸들을 사이즈별로 묶는다: { "M": {CHEST_WIDTH: 56, ...}, "L": {...} } (입력 순서 유지)
    public Map<String, Map<MeasurementPart, Double>> getSizes() {
        Map<String, Map<MeasurementPart, Double>> sizes = new LinkedHashMap<>();
        for (SizeChartEntry entry : entries) {
            sizes.computeIfAbsent(entry.getSizeLabel(), k -> new LinkedHashMap<>())
                    .put(entry.getPart(), entry.getValueCm());
        }
        return sizes;
    }
}
