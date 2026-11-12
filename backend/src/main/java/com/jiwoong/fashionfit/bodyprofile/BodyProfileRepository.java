package com.jiwoong.fashionfit.bodyprofile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyProfileRepository extends JpaRepository<BodyProfile, Long> {

    // user.id 로 찾는다 (메서드 이름의 UserId = user 필드의 id)
    Optional<BodyProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
