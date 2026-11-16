package com.jiwoong.fashionfit.sizecheck;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.common.NotFoundException;
import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.GarmentMeasurement;
import com.jiwoong.fashionfit.garment.MeasurementPart;
import com.jiwoong.fashionfit.garment.ReferenceGarment;
import com.jiwoong.fashionfit.garment.ReferenceGarmentRepository;
import com.jiwoong.fashionfit.sizecheck.SizeCheckRequest.SizeInput;
import com.jiwoong.fashionfit.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SizeCheckService {

    private static final double MIN_CM = 1;
    private static final double MAX_CM = 200;

    private final SizeChartRepository sizeChartRepository;
    private final ReferenceGarmentRepository garmentRepository;
    private final UserRepository userRepository;
    private final SizeRecommender sizeRecommender;

    // 실측표를 저장하고, 기준 옷과 비교한 결과를 돌려준다
    @Transactional
    public SizeCheckResponse create(Long userId, SizeCheckRequest request) {
        List<SizeChartEntry> entries = validateAndFlatten(request.category(), request.sizes());
        ReferenceGarment reference = request.referenceGarmentId() != null
                ? findRequestedReference(userId, request.referenceGarmentId(), request.category())
                : pickReference(userId, request.category());

        SizeChart chart = sizeChartRepository.save(SizeChart.create(
                userRepository.getReferenceById(userId), request.category(),
                request.productName().trim(), blankToNull(request.productUrl()), entries));

        SizeRecommendation result = sizeRecommender.recommend(measurementsOf(reference), chart.getSizes());
        chart.recordResult(
                reference == null ? null : reference.getId(),
                reference == null ? null : reference.getName(),
                result.recommendedSize());
        return SizeCheckResponse.of(chart, reference, result);
    }

    public List<SizeCheckSummary> list(Long userId) {
        return sizeChartRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SizeCheckSummary::from)
                .toList();
    }

    // 다시 볼 때는 비교 당시의 기준 옷으로 다시 계산한다.
    // 그 옷이 지워졌으면 지금 옷장에서 다시 고른다 (기준 옷 실측을 고쳤으면 고친 값이 반영된다).
    public SizeCheckResponse get(Long userId, Long sizeCheckId) {
        SizeChart chart = findOwned(userId, sizeCheckId);
        ReferenceGarment reference = chart.getReferenceGarmentId() == null ? null
                : garmentRepository.findByIdAndUserId(chart.getReferenceGarmentId(), userId).orElse(null);
        if (reference == null) {
            reference = pickReference(userId, chart.getCategory());
        }
        SizeRecommendation result = sizeRecommender.recommend(measurementsOf(reference), chart.getSizes());
        return SizeCheckResponse.of(chart, reference, result);
    }

    @Transactional
    public void delete(Long userId, Long sizeCheckId) {
        sizeChartRepository.delete(findOwned(userId, sizeCheckId));
    }

    private SizeChart findOwned(Long userId, Long sizeCheckId) {
        return sizeChartRepository.findByIdAndUserId(sizeCheckId, userId)
                .orElseThrow(() -> new NotFoundException("비교 기록을 찾을 수 없어요"));
    }

    // 기준 옷 자동 선택: ① 같은 카테고리 중 최근 것 ② 없으면 재는 부위가 같은 카테고리(예: 반팔↔긴팔) 중 최근 것
    private ReferenceGarment pickReference(Long userId, GarmentCategory category) {
        List<ReferenceGarment> garments = garmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return garments.stream()
                .filter(g -> g.getCategory() == category)
                .findFirst()
                .or(() -> garments.stream().filter(g -> isComparable(g.getCategory(), category)).findFirst())
                .orElse(null);
    }

    private ReferenceGarment findRequestedReference(Long userId, Long garmentId, GarmentCategory category) {
        ReferenceGarment garment = garmentRepository.findByIdAndUserId(garmentId, userId)
                .orElseThrow(() -> new NotFoundException("기준 옷을 찾을 수 없어요"));
        if (!isComparable(garment.getCategory(), category)) {
            throw new InvalidInputException("기준 옷을 확인해 주세요",
                    Map.of("referenceGarmentId", "상의는 상의끼리, 바지는 바지끼리 비교할 수 있어요"));
        }
        return garment;
    }

    private boolean isComparable(GarmentCategory a, GarmentCategory b) {
        return a.getParts().equals(b.getParts());
    }

    private Map<MeasurementPart, Double> measurementsOf(ReferenceGarment garment) {
        if (garment == null) {
            return null;
        }
        Map<MeasurementPart, Double> values = new EnumMap<>(MeasurementPart.class);
        for (GarmentMeasurement m : garment.getMeasurements()) {
            values.put(m.getPart(), m.getValueCm());
        }
        return values;
    }

    // 규칙: 사이즈 이름은 겹치면 안 되고, 사이즈마다 실측이 하나 이상, 부위는 카테고리에 있는 것만, 값은 1~200cm.
    // 쇼핑몰마다 표기하는 부위가 달라서 "모든 부위 필수"로 하지 않는다. (기준 옷 등록과 다른 점)
    private List<SizeChartEntry> validateAndFlatten(GarmentCategory category, List<SizeInput> sizes) {
        Map<String, String> errors = new LinkedHashMap<>();
        Set<String> seenLabels = new HashSet<>();
        List<SizeChartEntry> entries = new ArrayList<>();

        for (int i = 0; i < sizes.size(); i++) {
            SizeInput size = sizes.get(i);
            String prefix = "sizes[" + i + "]";
            String label = size.label().trim();

            if (!seenLabels.add(label.toUpperCase())) {
                errors.put(prefix + ".label", "'" + label + "' 사이즈가 두 번 입력됐어요");
            }
            if (size.measurements().isEmpty()) {
                errors.put(prefix, label + " 사이즈의 실측을 하나 이상 입력해 주세요");
            }
            for (Map.Entry<MeasurementPart, Double> m : size.measurements().entrySet()) {
                MeasurementPart part = m.getKey();
                Double value = m.getValue();
                String key = prefix + ".measurements." + part.name();
                if (!category.getParts().contains(part)) {
                    errors.put(key, category.getLabel() + "에는 필요 없는 항목이에요");
                } else if (value == null || value < MIN_CM || value > MAX_CM) {
                    errors.put(key, part.getLabel() + " 값은 1~200cm 사이로 입력해 주세요");
                } else {
                    entries.add(new SizeChartEntry(label, part, Math.round(value * 10) / 10.0));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("실측표를 확인해 주세요", errors);
        }
        return entries;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
