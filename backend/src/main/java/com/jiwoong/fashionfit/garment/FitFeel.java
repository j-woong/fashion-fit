package com.jiwoong.fashionfit.garment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 기준 옷을 "어떻게 입는지". 같은 실측이라도 오버핏으로 입는 사람과 딱 맞게 입는 사람은 추천이 달라야 한다.
@Getter
@RequiredArgsConstructor
public enum FitFeel {
    SNUG("몸에 붙게 입어요"),
    REGULAR("딱 맞아요"),
    RELAXED("살짝 넉넉해요"),
    OVERSIZED("오버핏으로 입어요");

    private final String label;
}
