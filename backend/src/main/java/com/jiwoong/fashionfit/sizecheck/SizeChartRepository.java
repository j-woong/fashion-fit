package com.jiwoong.fashionfit.sizecheck;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeChartRepository extends JpaRepository<SizeChart, Long> {

    // 목록은 요약 컬럼만 쓰므로 entries 를 불러오지 않는다
    List<SizeChart> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "entries")
    Optional<SizeChart> findByIdAndUserId(Long id, Long userId);

    // 같은 상품 링크로 저장한 실측표 중 가장 최근 것 (V3 인덱스 idx_size_chart_user_url 을 탄다)
    // @EntityGraph 를 붙이지 않았다: 컬렉션 조인 + LIMIT 는 Hibernate 가 메모리에서 잘라서 경고(HHH90003004)가 난다.
    // 한 건뿐이라 entries 는 필요할 때 쿼리 한 번 더 나가는 것으로 충분하다.
    Optional<SizeChart> findFirstByUserIdAndProductUrlOrderByCreatedAtDesc(Long userId, String productUrl);
}
