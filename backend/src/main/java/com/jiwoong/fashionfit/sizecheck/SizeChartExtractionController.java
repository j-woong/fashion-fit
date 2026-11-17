package com.jiwoong.fashionfit.sizecheck;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jiwoong.fashionfit.garment.GarmentCategory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 실측표 → 입력칸에 채울 값. 저장하지 않으므로 size-checks 와 주소를 분리했다.
//   POST /api/me/size-chart-extractions        (multipart: image, category)  스크린샷, LLM
//   POST /api/me/size-chart-extractions/text   (json: text, category)        글자, 즉시
@RestController
@RequestMapping("/api/me/size-chart-extractions")
@RequiredArgsConstructor
public class SizeChartExtractionController {

    private final SizeChartExtractionService extractionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SizeChartExtractionResponse extract(@RequestPart("image") MultipartFile image,
                                               @RequestParam("category") GarmentCategory category) throws IOException {
        return extractionService.extract(image.getBytes(), image.getContentType(), category);
    }

    @PostMapping(path = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SizeChartExtractionResponse extractFromText(@Valid @RequestBody SizeChartTextRequest request) {
        return extractionService.extractFromText(request.text(), request.category());
    }
}
