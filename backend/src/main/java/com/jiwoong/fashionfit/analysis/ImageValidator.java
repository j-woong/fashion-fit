package com.jiwoong.fashionfit.analysis;

import java.util.Map;
import java.util.Set;

import com.jiwoong.fashionfit.common.InvalidInputException;

// 업로드 이미지 공통 검사 (옷 스크린샷, 실측표 스크린샷이 같이 쓴다)
public final class ImageValidator {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    private ImageValidator() {
    }

    public static void validate(byte[] image, String contentType) {
        if (image == null || image.length == 0) {
            throw new InvalidInputException("이미지를 확인해 주세요", Map.of("image", "스크린샷을 올려 주세요"));
        }
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidInputException("이미지를 확인해 주세요", Map.of("image", "PNG, JPG, WEBP 이미지만 올릴 수 있어요"));
        }
        if (image.length > MAX_BYTES) {
            throw new InvalidInputException("이미지를 확인해 주세요", Map.of("image", "5MB 이하 이미지만 올릴 수 있어요"));
        }
    }
}
