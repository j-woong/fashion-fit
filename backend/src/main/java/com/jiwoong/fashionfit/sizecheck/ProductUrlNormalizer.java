package com.jiwoong.fashionfit.sizecheck;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

// 같은 상품인데 주소가 조금씩 달라서(인스타 광고의 utm_... 꼬리표 등) 못 찾는 일을 막는다.
// 저장할 때와 찾을 때 똑같이 정리해서, DB 에서는 단순히 "같은 글자인지"만 비교하면 된다.
// (찾을 때마다 LIKE 로 비슷한 주소를 뒤지는 방법도 있지만, 인덱스를 못 타고 엉뚱한 상품이 걸릴 수 있다)
public final class ProductUrlNormalizer {

    // 상품을 가리키지 않는 추적용 파라미터. product_no 같은 진짜 파라미터는 남겨야 한다.
    private static final Set<String> TRACKING_PARAMS = Set.of("fbclid", "gclid", "igshid", "igsh", "srsltid");

    private ProductUrlNormalizer() {
    }

    // 추적 파라미터와 #뒤를 뗀 주소. 비어 있으면 null.
    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.strip();
        int hash = trimmed.indexOf('#');
        if (hash >= 0) {
            trimmed = trimmed.substring(0, hash);
        }
        int question = trimmed.indexOf('?');
        if (question < 0) {
            return trimmed;
        }
        String base = trimmed.substring(0, question);
        String query = Arrays.stream(trimmed.substring(question + 1).split("&"))
                .filter(param -> !param.isEmpty() && !isTracking(param))
                .collect(Collectors.joining("&"));
        return query.isEmpty() ? base : base + "?" + query;
    }

    // "https://shop.com/" 처럼 쇼핑몰 첫 화면 주소인지. 이런 주소는 상품을 구분하지 못해서 찾기에 쓰지 않는다.
    public static boolean isHomepage(String normalizedUrl) {
        String withoutScheme = normalizedUrl.replaceFirst("(?i)^https?://", "");
        int slash = withoutScheme.indexOf('/');
        return slash < 0 || withoutScheme.substring(slash).equals("/");
    }

    private static boolean isTracking(String param) {
        String name = param.split("=", 2)[0].toLowerCase();
        return name.startsWith("utm_") || TRACKING_PARAMS.contains(name);
    }
}
