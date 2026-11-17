package com.jiwoong.fashionfit.analysis;

/**
 * 옷 사진 → GarmentAnalysis. 어떤 LLM 을 쓰는지는 구현체가 정한다.
 *
 * <p>인터페이스로 분리한 이유
 * <ul>
 *   <li>LLM 제공사(Gemini → Claude/OpenAI)를 바꿔도 서비스 코드는 그대로 둔다.</li>
 *   <li>테스트에서 실제 API 를 부르지 않고 가짜 구현으로 서비스 규칙만 검증할 수 있다.</li>
 * </ul>
 */
public interface GarmentImageAnalyzer {

    GarmentAnalysis analyze(byte[] image, String contentType);
}
