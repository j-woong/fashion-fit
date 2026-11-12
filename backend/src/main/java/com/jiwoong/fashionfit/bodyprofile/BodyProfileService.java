package com.jiwoong.fashionfit.bodyprofile;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodyProfileService {

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;

    public Optional<BodyProfileResponse> find(Long userId) {
        return bodyProfileRepository.findByUserId(userId).map(BodyProfileResponse::from);
    }

    public boolean exists(Long userId) {
        return bodyProfileRepository.existsByUserId(userId);
    }

    // 있으면 수정, 없으면 새로 만든다 (upsert). 그래서 HTTP 메서드도 PUT.
    // 수정은 save() 를 안 불러도 트랜잭션이 끝날 때 변경 감지(dirty checking)로 UPDATE 된다.
    @Transactional
    public BodyProfileResponse save(Long userId, BodyProfileRequest request) {
        return bodyProfileRepository.findByUserId(userId)
                .map(profile -> {
                    profile.update(request.heightCm(), request.weightKg(), request.footSizeMm());
                    return BodyProfileResponse.from(profile);
                })
                .orElseGet(() -> BodyProfileResponse.from(bodyProfileRepository.save(
                        BodyProfile.create(userRepository.getReferenceById(userId),
                                request.heightCm(), request.weightKg(), request.footSizeMm()))));
    }
}
