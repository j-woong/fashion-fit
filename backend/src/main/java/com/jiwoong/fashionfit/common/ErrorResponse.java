package com.jiwoong.fashionfit.common;

import java.util.Map;

// 모든 에러 응답의 모양을 통일한다. 프론트의 src/api/client.ts ErrorResponse 와 짝.
// fieldErrors: { "footSizeMm": "발 사이즈는 꼭 입력해 주세요" } 처럼 입력칸별 메시지
public record ErrorResponse(String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, Map.of());
    }
}
