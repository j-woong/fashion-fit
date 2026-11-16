package com.jiwoong.fashionfit.sizecheck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 추천 결과를 한 마디로. 기준은 "가중 평균 차이(cm)" = SizeRecommender 의 score
@Getter
@RequiredArgsConstructor
public enum Verdict {
    GOOD("기준 옷과 거의 같아요"),
    SLIGHTLY_OFF("기준 옷과 조금 차이가 있어요"),
    FAR("어느 사이즈도 기준 옷과 차이가 커요"),
    NO_REFERENCE("이 종류의 기준 옷이 없어서 비교할 수 없어요"),
    NOT_COMPARABLE("기준 옷과 같이 잰 부위가 없어서 비교할 수 없어요");

    private final String label;
}
