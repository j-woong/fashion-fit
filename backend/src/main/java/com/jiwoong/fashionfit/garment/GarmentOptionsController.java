package com.jiwoong.fashionfit.garment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 등록 화면용 선택지: GET /api/garment-options
@RestController
public class GarmentOptionsController {

    private static final GarmentOptionsResponse OPTIONS = GarmentOptionsResponse.create();

    @GetMapping("/api/garment-options")
    public GarmentOptionsResponse options() {
        return OPTIONS;
    }
}
