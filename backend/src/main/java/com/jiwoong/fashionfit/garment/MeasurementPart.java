package com.jiwoong.fashionfit.garment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 실측 부위. 기준 옷과(4단계) 상품 실측표(나중 단계)가 같은 enum 을 써서 그대로 비교할 수 있다.
// guide: 화면에서 "어디부터 어디까지 재는지" 안내 문구로 쓴다.
@Getter
@RequiredArgsConstructor
public enum MeasurementPart {
    TOTAL_LENGTH("총장", "상의는 뒷목 아래, 바지는 허리 위부터 밑단까지"),
    SHOULDER_WIDTH("어깨너비", "어깨 솔기 끝에서 끝까지"),
    CHEST_WIDTH("가슴단면", "겨드랑이 바로 아래 끝에서 끝까지"),
    SLEEVE_LENGTH("소매길이", "어깨 솔기부터 소매 끝까지"),
    WAIST_WIDTH("허리단면", "허리 밴드 한쪽 끝에서 끝까지"),
    HIP_WIDTH("엉덩이단면", "엉덩이에서 가장 넓은 부분 끝에서 끝까지"),
    THIGH_WIDTH("허벅지단면", "가랑이 바로 아래 끝에서 끝까지"),
    RISE("밑위", "허리 위에서 가랑이 이음새까지"),
    HEM_WIDTH("밑단단면", "바지 밑단 끝에서 끝까지");

    private final String label;
    private final String guide;
}
