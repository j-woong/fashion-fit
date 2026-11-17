package com.jiwoong.fashionfit.sizecheck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 내 사이즈와 비교했을 때 이 사이즈가 어떤 핏으로 입혀지는지.
// 기준: 품(가슴·어깨·허리·엉덩이·허벅지 단면)의 평균 차이(cm). 단면 1cm 는 둘레로 약 2cm 다.
@Getter
@RequiredArgsConstructor
public enum FitType {
    TIGHT("타이트", "내 사이즈보다 작아서 끼일 수 있어요"),
    SLIM("슬림핏", "내 사이즈보다 살짝 붙는 슬림핏이에요"),
    REGULAR("레귤러핏", "내 사이즈와 비슷한 레귤러핏이에요"),
    SEMI_OVER("세미오버핏", "내 사이즈보다 여유 있는 세미오버핏이에요"),
    OVER("오버핏", "내 사이즈보다 많이 넉넉한 오버핏이에요");

    private final String label;
    private final String description;

    public static FitType of(double avgWidthDiffCm) {
        if (avgWidthDiffCm < -3) {
            return TIGHT;
        }
        if (avgWidthDiffCm < -1) {
            return SLIM;
        }
        if (avgWidthDiffCm <= 1.5) {
            return REGULAR;
        }
        if (avgWidthDiffCm <= 4) {
            return SEMI_OVER;
        }
        return OVER;
    }
}
