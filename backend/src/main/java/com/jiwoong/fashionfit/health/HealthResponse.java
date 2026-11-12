package com.jiwoong.fashionfit.health;

import java.time.LocalDateTime;

// 프론트의 src/api/health.ts 에 있는 HealthResponse 타입과 모양을 맞춘다.
public record HealthResponse(String status, LocalDateTime serverTime) {
}
