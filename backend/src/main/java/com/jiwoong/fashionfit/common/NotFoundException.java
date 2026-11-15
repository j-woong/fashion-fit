package com.jiwoong.fashionfit.common;

// 찾는 데이터가 없을 때 → 404. 남의 데이터에 접근했을 때도 "없음"으로 처리한다 (있다는 사실도 숨김).
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
