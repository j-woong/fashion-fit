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
}
