package com.jiwoong.fashionfit.common;

import java.util.Map;

// @Valid 만으로는 검사할 수 없는 규칙(예: "바지면 허리단면이 필수")을 서비스에서 검사하다 걸렸을 때 → 400.
// @Valid 실패와 똑같은 모양(ErrorResponse)으로 내려가서 프론트는 둘을 구분하지 않아도 된다.
public class InvalidInputException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public InvalidInputException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
