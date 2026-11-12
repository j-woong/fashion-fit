package com.jiwoong.fashionfit.bodyprofile;

// 프론트의 src/api/bodyProfile.ts 에 있는 BodyProfile 타입과 짝
public record BodyProfileResponse(Integer heightCm, Integer weightKg, Integer footSizeMm) {

    public static BodyProfileResponse from(BodyProfile profile) {
        return new BodyProfileResponse(profile.getHeightCm(), profile.getWeightKg(), profile.getFootSizeMm());
    }
}
