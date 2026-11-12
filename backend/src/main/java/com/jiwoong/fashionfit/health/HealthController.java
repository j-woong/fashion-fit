package com.jiwoong.fashionfit.health;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 프론트가 서버 연결을 확인할 때 부르는 API: GET /api/health
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", LocalDateTime.now().withNano(0));
    }
}
