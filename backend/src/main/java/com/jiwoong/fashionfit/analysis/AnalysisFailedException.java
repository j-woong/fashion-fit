package com.jiwoong.fashionfit.analysis;

// 외부 LLM 호출이 실패했을 때 (네트워크, 한도 초과, 응답 형식 오류 등) → 502
public class AnalysisFailedException extends RuntimeException {

    public AnalysisFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
