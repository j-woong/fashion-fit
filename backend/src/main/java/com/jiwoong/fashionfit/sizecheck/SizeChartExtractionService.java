package com.jiwoong.fashionfit.sizecheck;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.jiwoong.fashionfit.analysis.AnalysisFailedException;
import com.jiwoong.fashionfit.analysis.ImageValidator;
import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.MeasurementPart;
import com.jiwoong.fashionfit.sizecheck.SizeChartExtractionResponse.SizeRow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 실측표(스크린샷 또는 글자)를 읽어 입력칸에 채울 값을 만든다. 저장은 하지 않는다.
// 읽은 값은 틀릴 수 있어서, 화면에서 사용자가 확인·수정한 뒤 "비교하기"를 눌러야 저장된다.
//   글자   → SizeChartTextParser (정규식, 즉시)
//   이미지 → SizeChartImageReader (LLM, 10초 안팎)
// 어느 쪽으로 읽었든 "카테고리에 맞는 부위만, 1~200cm 만" 남기는 규칙은 여기 한 곳에서 똑같이 적용한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class SizeChartExtractionService {

    static final int MAX_ROWS = 10;
    private static final double MIN_CM = 1;
    private static final double MAX_CM = 200;

    private final SizeChartImageReader reader;

    public SizeChartExtractionResponse extract(byte[] image, String contentType, GarmentCategory category) {
        ImageValidator.validate(image, contentType);

        SizeChartReading reading;
        try {
            reading = reader.read(image, contentType, category);
        } catch (RuntimeException e) {
            log.warn("실측표 읽기 실패: {}", e.getMessage(), e);
            throw new AnalysisFailedException("실측표를 읽지 못했어요. 잠시 후 다시 시도하거나 직접 입력해 주세요.", e);
        }

        List<SizeRow> sizes = reading == null || !reading.sizeChart() || reading.rows() == null
                ? List.of()
                : reading.rows().stream()
                        .filter(Objects::nonNull)
                        .map(row -> toSizeRow(row.label(), part -> valueOf(row, part), category))
                        .filter(row -> !row.label().isEmpty() && !row.measurements().isEmpty())
                        .limit(MAX_ROWS)
                        .toList();

        if (sizes.isEmpty()) {
            throw new InvalidInputException("실측표를 찾지 못했어요",
                    Map.of("image", "실측표(사이즈표)가 잘 보이는 스크린샷을 올려 주세요"));
        }
        return new SizeChartExtractionResponse(sizes);
    }

    // 상품 설명에서 복사한 글자에서 읽는다. LLM 을 부르지 않는다.
    public SizeChartExtractionResponse extractFromText(String text, GarmentCategory category) {
        List<SizeRow> sizes = SizeChartTextParser.parse(text).entrySet().stream()
                .map(entry -> toSizeRow(entry.getKey(), entry.getValue()::get, category))
                .filter(row -> !row.label().isEmpty() && !row.measurements().isEmpty())
                .limit(MAX_ROWS)
                .toList();

        if (sizes.isEmpty()) {
            throw new InvalidInputException("실측을 찾지 못했어요",
                    Map.of("text", "'M 어깨 52 가슴 58'처럼 사이즈와 숫자가 적힌 부분을 붙여 넣어 주세요"));
        }
        return new SizeChartExtractionResponse(sizes);
    }

    // 이 카테고리에 필요한 부위만, 말이 되는 값(1~200cm)만 남긴다
    private SizeRow toSizeRow(String rawLabel, Function<MeasurementPart, Double> valueOf, GarmentCategory category) {
        Map<MeasurementPart, Double> measurements = new EnumMap<>(MeasurementPart.class);
        for (MeasurementPart part : category.getParts()) {
            Double value = valueOf.apply(part);
            if (value != null && value >= MIN_CM && value <= MAX_CM) {
                measurements.put(part, Math.round(value * 10) / 10.0);
            }
        }
        String label = rawLabel == null ? "" : rawLabel.trim();
        return new SizeRow(label.length() > 20 ? label.substring(0, 20) : label, measurements);
    }

    private Double valueOf(SizeChartReading.Row row, MeasurementPart part) {
        return switch (part) {
            case TOTAL_LENGTH -> row.totalLength();
            case SHOULDER_WIDTH -> row.shoulderWidth();
            case CHEST_WIDTH -> row.chestWidth();
            case SLEEVE_LENGTH -> row.sleeveLength();
            case WAIST_WIDTH -> row.waistWidth();
            case HIP_WIDTH -> row.hipWidth();
            case THIGH_WIDTH -> row.thighWidth();
            case RISE -> row.rise();
            case HEM_WIDTH -> row.hemWidth();
        };
    }
}
