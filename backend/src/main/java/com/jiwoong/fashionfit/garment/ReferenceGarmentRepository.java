package com.jiwoong.fashionfit.garment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceGarmentRepository extends JpaRepository<ReferenceGarment, Long> {

    // @EntityGraph: 옷 목록을 가져올 때 실측도 한 번의 쿼리로 같이 가져온다.
    // 없으면 옷 N개를 보여줄 때 실측 쿼리가 N번 더 나간다 (N+1 문제).
    @EntityGraph(attributePaths = "measurements")
    List<ReferenceGarment> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // id 만으로 찾지 않고 userId 도 같이 건다: 남의 옷 id 를 넣어도 "없음"이 된다.
    @EntityGraph(attributePaths = "measurements")
    Optional<ReferenceGarment> findByIdAndUserId(Long id, Long userId);
}
