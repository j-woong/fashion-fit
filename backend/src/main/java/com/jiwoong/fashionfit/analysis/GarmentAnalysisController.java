package com.jiwoong.fashionfit.analysis;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

// POST /api/me/garment-analyses  (multipart/form-data, 필드 이름: image)
// /api/me 아래라서 로그인한 사용자만 쓸 수 있다 (무료 LLM 호출 한도를 아무나 쓰지 못하게)
@RestController
@RequestMapping("/api/me/garment-analyses")
@RequiredArgsConstructor
public class GarmentAnalysisController {

    private final GarmentAnalysisService analysisService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GarmentAnalysisResponse analyze(@RequestPart("image") MultipartFile image) throws IOException {
        return analysisService.analyze(image.getBytes(), image.getContentType());
    }
}
