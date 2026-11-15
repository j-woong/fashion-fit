package com.jiwoong.fashionfit.garment;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.common.NotFoundException;
import com.jiwoong.fashionfit.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarmentService {

    private static final double MIN_CM = 1;
    private static final double MAX_CM = 200;

    private final ReferenceGarmentRepository garmentRepository;
    private final UserRepository userRepository;

    public List<GarmentResponse> list(Long userId) {
        return garmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(GarmentResponse::from)
                .toList();
    }

    public GarmentResponse get(Long userId, Long garmentId) {
        return GarmentResponse.from(findOwned(userId, garmentId));
    }

    @Transactional
    public GarmentResponse create(Long userId, GarmentRequest request) {
        Map<MeasurementPart, Double> values = validateMeasurements(request.category(), request.measurements());
        ReferenceGarment garment = ReferenceGarment.create(
                userRepository.getReferenceById(userId),
                request.category(), request.name().trim(), request.fitFeel(), values);
        // 실측은 cascade 로 같이 저장된다
        return GarmentResponse.from(garmentRepository.save(garment));
    }

    @Transactional
    public GarmentResponse update(Long userId, Long garmentId, GarmentRequest request) {
        ReferenceGarment garment = findOwned(userId, garmentId);
        Map<MeasurementPart, Double> values = validateMeasurements(request.category(), request.measurements());
        garment.update(request.category(), request.name().trim(), request.fitFeel(), values);
        return GarmentResponse.from(garment);
    }

    @Transactional
    public void delete(Long userId, Long garmentId) {
        // 실측은 orphanRemoval 로 같이 지워진다
        garmentRepository.delete(findOwned(userId, garmentId));
    }

    private ReferenceGarment findOwned(Long userId, Long garmentId) {
        return garmentRepository.findByIdAndUserId(garmentId, userId)
                .orElseThrow(() -> new NotFoundException("옷을 찾을 수 없어요"));
    }

    // 카테고리에 필요한 부위가 모두 있고, 값이 1~200cm 인지 검사한다.
    // 에러 키를 "measurements.CHEST_WIDTH" 로 만들어서 프론트가 해당 입력칸 아래에 메시지를 붙일 수 있게 한다.
    private Map<MeasurementPart, Double> validateMeasurements(GarmentCategory category,
                                                              Map<MeasurementPart, Double> input) {
        Map<String, String> errors = new LinkedHashMap<>();
        Map<MeasurementPart, Double> result = new EnumMap<>(MeasurementPart.class);

        for (MeasurementPart part : category.getParts()) {
            Double value = input.get(part);
            String key = "measurements." + part.name();
            if (value == null) {
                errors.put(key, part.getLabel() + " 값을 입력해 주세요");
            } else if (value < MIN_CM || value > MAX_CM) {
                errors.put(key, part.getLabel() + " 값은 1~200cm 사이로 입력해 주세요");
            } else {
                result.put(part, Math.round(value * 10) / 10.0); // 소수점 한 자리까지만
            }
        }
        for (MeasurementPart part : input.keySet()) {
            if (!category.getParts().contains(part)) {
                errors.put("measurements." + part.name(), category.getLabel() + "에는 필요 없는 항목이에요");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("실측값을 확인해 주세요", errors);
        }
        return result;
    }
}
